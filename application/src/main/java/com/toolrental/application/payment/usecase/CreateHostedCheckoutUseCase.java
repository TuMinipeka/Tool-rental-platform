package com.toolrental.application.payment.usecase;

import com.toolrental.application.common.exception.IdempotencyConflictApplicationException;
import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.payment.dto.HostedCheckoutResponse;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.payment.model.aggregate.Payment;
import com.toolrental.domain.payment.model.valueobject.PaymentId;
import com.toolrental.domain.payment.port.repository.HostedCheckoutRepository;
import com.toolrental.domain.payment.port.repository.PaymentRepository;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import com.toolrental.domain.reservation.port.repository.AtomicReservationManagement;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public final class CreateHostedCheckoutUseCase {
  private final PaymentRepository payments;
  private final HostedCheckoutRepository checkouts;
  private final WorkflowReferences references;
  private final AccessPolicy access;
  private final UnitOfWork work;
  private final AtomicReservationManagement reservations;
  private final ReconcileHostedPaymentUseCase reconcile;
  private final Clock clock;

  public CreateHostedCheckoutUseCase(
      PaymentRepository payments,
      HostedCheckoutRepository checkouts,
      WorkflowReferences references,
      AccessPolicy access,
      UnitOfWork work,
      AtomicReservationManagement reservations,
      ReconcileHostedPaymentUseCase reconcile,
      Clock clock) {
    this.payments = payments;
    this.checkouts = checkouts;
    this.references = references;
    this.access = access;
    this.work = work;
    this.reservations = reservations;
    this.reconcile = reconcile;
    this.clock = clock;
  }

  public HostedCheckoutResponse execute(UUID actor, ReservationId reservationId, String key) {
    if (key == null || key.isBlank() || key.length() > 200)
      throw new IllegalArgumentException("Invalid idempotency key");
    UUID id =
        work.execute(
            () -> {
              access.requireRole(actor, "CUSTOMER");
              var owned = references.reservation(actor, reservationId, false);
              if (!owned.customerId().value().equals(actor))
                throw new BusinessRuleException("Only the customer can pay");
              var result = new AtomicReference<UUID>();
              reservations.execute(
                  reservationId,
                  context -> {
                    var reservation = context.reservation();
                    var previous = payments.findByIdempotencyKey(key);
                    if (previous.isPresent()) {
                      var stored = previous.get();
                      if (!stored.reservationId().equals(reservationId)
                          || !stored.processor().equals("MERCADO_PAGO"))
                        throw new IdempotencyConflictApplicationException();
                      result.set(stored.id().value());
                      return reservation;
                    }
                    var active =
                        payments.findByReservation(reservationId).stream()
                            .filter(p -> p.processor().equals("MERCADO_PAGO"))
                            .findFirst();
                    if (active.isPresent()) {
                      result.set(active.get().id().value());
                      return reservation;
                    }
                    if (reservation.status() != ReservationStatus.PENDING_PAYMENT
                        || !Instant.now(clock).isBefore(reservation.expiresAt()))
                      throw new BusinessRuleException("Payment hold has expired or is not payable");
                    if (!reservation.total().currency().getCurrencyCode().equals("COP"))
                      throw new BusinessRuleException("Only COP is supported");
                    reservation.total().amount().setScale(2, java.math.RoundingMode.UNNECESSARY);
                    var payment =
                        payments.save(
                            Payment.hostedCheckout(
                                PaymentId.generate(),
                                reservationId,
                                reservation.total().amount(),
                                key,
                                clock));
                    checkouts.initialize(payment.id().value(), reservation.expiresAt());
                    result.set(payment.id().value());
                    return reservation;
                  });
              return result.get();
            });
    reconcile.execute(id);
    return HostedCheckoutResponse.from(checkouts.find(id).orElseThrow());
  }
}
