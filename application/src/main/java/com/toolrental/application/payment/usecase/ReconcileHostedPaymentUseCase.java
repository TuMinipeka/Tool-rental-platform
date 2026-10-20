package com.toolrental.application.payment.usecase;

import com.toolrental.application.invoice.usecase.IssueAcademicInvoiceUseCase;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.payment.model.valueobject.PaymentId;
import com.toolrental.domain.payment.port.PaymentGateway;
import com.toolrental.domain.payment.port.repository.HostedCheckoutRepository;
import com.toolrental.domain.payment.port.repository.PaymentRepository;
import com.toolrental.domain.refund.model.aggregate.Refund;
import com.toolrental.domain.refund.model.valueobject.RefundId;
import com.toolrental.domain.refund.port.repository.RefundRepository;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import com.toolrental.domain.reservation.port.repository.AtomicReservationManagement;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public final class ReconcileHostedPaymentUseCase {
  private final PaymentGateway gateway;
  private final HostedCheckoutRepository checkouts;
  private final PaymentRepository payments;
  private final AtomicReservationManagement reservations;
  private final IssueAcademicInvoiceUseCase invoices;
  private final UnitOfWork work;
  private final Clock clock;
  private final String collectorId;
  private final RefundRepository refunds;

  public ReconcileHostedPaymentUseCase(
      PaymentGateway gateway,
      HostedCheckoutRepository checkouts,
      PaymentRepository payments,
      AtomicReservationManagement reservations,
      IssueAcademicInvoiceUseCase invoices,
      UnitOfWork work,
      Clock clock,
      String collectorId,
      RefundRepository refunds) {
    this.gateway = gateway;
    this.checkouts = checkouts;
    this.payments = payments;
    this.reservations = reservations;
    this.invoices = invoices;
    this.work = work;
    this.clock = clock;
    this.collectorId = collectorId;
    this.refunds = refunds;
  }

  public void execute(UUID id) {
    run(id, null);
  }

  public void webhook(String providerPaymentId) {
    var result = gateway.fetch(providerPaymentId);
    UUID id;
    try {
      id = UUID.fromString(result.reference());
    } catch (RuntimeException invalid) {
      throw new IllegalArgumentException("Unknown payment reference");
    }
    run(id, result);
  }

  private void run(UUID id, PaymentGateway.Result notification) {
    var initial =
        checkouts.find(id).orElseThrow(() -> new IllegalArgumentException("Unknown checkout"));
    if (notification != null) validate(id, initial.amount(), notification);
    var now = Instant.now(clock);
    if (!checkouts.claim(id, now, now.plusSeconds(90))) {
      if (notification != null)
        throw new BusinessRuleException(
            "Checkout reconciliation is in progress; retry the notification");
      return;
    }
    try {
      var checkout = checkouts.find(id).orElseThrow();
      if (notification != null) {
        validate(id, checkout.amount(), notification);
        apply(id, notification);
      } else {
        var results = gateway.search(id.toString());
        for (var result : results) validate(id, checkout.amount(), result);
        var approved =
            results.stream()
                .filter(p -> p.status().equals("approved") || p.status().equals("refunded"))
                .toList();
        if (approved.size() > 1)
          throw new BusinessRuleException("Multiple provider payments require review");
        if (!approved.isEmpty()) apply(id, approved.getFirst());
        else if (!results.isEmpty()) apply(id, results.getFirst());
        else if (checkout.preferenceId() == null
            && checkout.attempts() == 1
            && now.isBefore(checkout.expiresAt())) {
          var preference = gateway.create(id.toString(), checkout.amount(), checkout.expiresAt());
          checkouts.ready(id, preference.preferenceId(), preference.url());
        } else if (checkout.preferenceId() == null) {
          // Recover the preference instead of repeating a possibly accepted external request.
          var recovered = gateway.recoverCheckout(id.toString());
          if (recovered.isPresent())
            checkouts.ready(id, recovered.get().preferenceId(), recovered.get().url());
          else checkouts.phase(id, "REVIEW", null, now.plusSeconds(300));
        }
      }
      var current = checkouts.find(id).orElseThrow();
      if (current.phase().equals("REFUND_PENDING")) {
        gateway.refund(current.providerPaymentId(), current.amount(), "late-" + id);
        work.execute(
            () -> {
              var payment = payments.findById(new PaymentId(id)).orElseThrow();
              if (payment.paymentStatus().equals("CONFIRMED"))
                payments.save(payment.transition("REFUNDED", clock));
              refunds
                  .findByIdempotencyKey("late-" + id)
                  .ifPresent(refund -> refunds.save(refund.confirm(Instant.now(clock))));
              checkouts.phase(id, "REFUNDED", current.providerPaymentId(), null);
              return null;
            });
      }
      var updated = checkouts.find(id).orElseThrow();
      if (updated.phase().equals("READY") || updated.phase().equals("CREATING"))
        checkouts.phase(id, updated.phase(), null, now.plusSeconds(60));
    } catch (RuntimeException error) {
      var current = checkouts.find(id).orElseThrow();
      if (!java.util.Set.of("REFUNDED").contains(current.phase()))
        checkouts.phase(
            id,
            current.phase().equals("REFUND_PENDING") ? "REFUND_PENDING" : "REVIEW",
            null,
            now.plusSeconds(300));
      throw error;
    } finally {
      checkouts.release(id);
    }
  }

  private void validate(UUID id, java.math.BigDecimal amount, PaymentGateway.Result result) {
    if (result.live()
        || !collectorId.equals(result.collectorId())
        || !id.toString().equals(result.reference())
        || !"COP".equals(result.currency())
        || amount.compareTo(result.amount()) != 0)
      throw new BusinessRuleException("Provider payment does not match the sandbox checkout");
  }

  private void apply(UUID id, PaymentGateway.Result result) {
    var checkout = checkouts.find(id).orElseThrow();
    work.execute(
        () -> {
          reservations.execute(
              new ReservationId(checkout.reservationId()),
              context -> {
                var payment = payments.findById(new PaymentId(id)).orElseThrow();
                var current = checkouts.find(id).orElseThrow();
                if (current.providerPaymentId() != null
                    && !current.providerPaymentId().equals(result.id()))
                  throw new BusinessRuleException("Additional provider payment requires review");
                var reservation = context.reservation();
                if (payment.paymentStatus().equals("REFUNDED")) return reservation;
                if (result.status().equals("refunded")) {
                  if (!current.phase().equals("REFUND_PENDING"))
                    throw new BusinessRuleException("Unrequested provider refund requires review");
                  if (payment.paymentStatus().equals("CONFIRMED"))
                    payments.save(payment.transition("REFUNDED", clock));
                  refunds
                      .findByIdempotencyKey("late-" + id)
                      .ifPresent(refund -> refunds.save(refund.confirm(Instant.now(clock))));
                  checkouts.phase(id, "REFUNDED", result.id(), null);
                  return reservation;
                }
                if (result.status().equals("approved")) {
                  if (payment.paymentStatus().equals("CONFIRMED")) return reservation;
                  var wasPending = payment.paymentStatus().equals("PENDING");
                  payments.save(payment.settleHostedApproval(clock));
                  boolean available =
                      context.isCustomerActive(reservation.customerId())
                          && context.isSupplierActive(reservation.supplierId())
                          && reservation.items().stream()
                              .allMatch(
                                  item -> {
                                    var unit = context.equipmentUnit(item.equipmentUnitId());
                                    var tool = context.tool(unit.toolId());
                                    return unit.isOperational()
                                        && tool.published()
                                        && tool.supplierId().equals(reservation.supplierId())
                                        && !context.isOccupied(unit.id(), reservation.period());
                                  });
                  if (wasPending
                      && available
                      && reservation.status() == ReservationStatus.PENDING_PAYMENT
                      && Instant.now(clock).isBefore(reservation.expiresAt())) {
                    var confirmed = reservation.confirmPayment(clock);
                    invoices.execute(confirmed);
                    checkouts.phase(id, "DONE", result.id(), null);
                    return confirmed;
                  }
                  checkouts.phase(id, "REFUND_PENDING", result.id(), null);
                  var key = "late-" + id;
                  if (refunds.findByIdempotencyKey(key).isEmpty())
                    refunds.save(
                        Refund.pending(
                            RefundId.generate(),
                            payment.id(),
                            null,
                            new Money(payment.amount(), java.util.Currency.getInstance("COP")),
                            key,
                            Instant.now(clock)));
                  if (reservation.status() == ReservationStatus.PENDING_PAYMENT)
                    return Instant.now(clock).isBefore(reservation.expiresAt())
                        ? reservation.failPayment(clock)
                        : reservation.expire(clock);
                  return reservation;
                }
                if ((result.status().equals("rejected") || result.status().equals("cancelled"))
                    && payment.paymentStatus().equals("PENDING")) {
                  payments.save(payment.transition("FAILED", clock));
                  checkouts.phase(id, "REJECTED", result.id(), null);
                  return reservation.status() == ReservationStatus.PENDING_PAYMENT
                      ? reservation.failPayment(clock)
                      : reservation;
                }
                return reservation;
              });
          return null;
        });
  }

  public void reconcileDue() {
    for (var id : checkouts.due(Instant.now(clock), 20)) {
      try {
        execute(id);
      } catch (RuntimeException ignored) {
        System.getLogger(ReconcileHostedPaymentUseCase.class.getName())
            .log(System.Logger.Level.WARNING, "Payment reconciliation deferred for {0}", id);
      }
    }
  }
}
