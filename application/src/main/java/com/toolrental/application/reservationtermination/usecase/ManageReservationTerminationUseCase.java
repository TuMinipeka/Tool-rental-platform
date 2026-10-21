package com.toolrental.application.reservationtermination.usecase;

import com.toolrental.application.common.exception.IdempotencyConflictApplicationException;
import com.toolrental.application.common.exception.ReferenceNotFoundApplicationException;
import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.reservationtermination.dto.ReservationTerminationResponse;
import com.toolrental.domain.cancellationpolicy.port.repository.CancellationPolicyRepository;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.payment.model.aggregate.Payment;
import com.toolrental.domain.payment.model.valueobject.PaymentId;
import com.toolrental.domain.payment.port.repository.PaymentRepository;
import com.toolrental.domain.refund.model.aggregate.Refund;
import com.toolrental.domain.refund.model.valueobject.RefundId;
import com.toolrental.domain.refund.port.repository.RefundRepository;
import com.toolrental.domain.rental.model.valueobject.RentalId;
import com.toolrental.domain.rental.port.RentalLifecycle;
import com.toolrental.domain.rental.port.repository.RentalRepository;
import com.toolrental.domain.reservation.model.aggregate.Reservation;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import com.toolrental.domain.reservation.port.repository.AtomicReservationManagement;
import com.toolrental.domain.reservationtermination.model.aggregate.ReservationTermination;
import com.toolrental.domain.reservationtermination.model.valueobject.ReservationTerminationId;
import com.toolrental.domain.reservationtermination.port.repository.ReservationTerminationRepository;
import com.toolrental.domain.user.model.valueobject.UserId;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ManageReservationTerminationUseCase {
  private final ReservationTerminationRepository terminations;
  private final CancellationPolicyRepository policies;
  private final PaymentRepository payments;
  private final RefundRepository refunds;
  private final RentalRepository rentals;
  private final RentalLifecycle lifecycle;
  private final AtomicReservationManagement reservations;
  private final WorkflowReferences references;
  private final AccessPolicy access;
  private final UnitOfWork work;
  private final Clock clock;

  public ManageReservationTerminationUseCase(
      ReservationTerminationRepository terminations,
      CancellationPolicyRepository policies,
      PaymentRepository payments,
      RefundRepository refunds,
      RentalRepository rentals,
      RentalLifecycle lifecycle,
      AtomicReservationManagement reservations,
      WorkflowReferences references,
      AccessPolicy access,
      UnitOfWork work,
      Clock clock) {
    this.terminations = terminations;
    this.policies = policies;
    this.payments = payments;
    this.refunds = refunds;
    this.rentals = rentals;
    this.lifecycle = lifecycle;
    this.reservations = reservations;
    this.references = references;
    this.access = access;
    this.work = work;
    this.clock = clock;
  }

  public ReservationTerminationResponse cancel(UUID actor, ReservationId id, String key) {
    return work.execute(() -> applyDirect(actor, id, "STANDARD", null, key));
  }

  public ReservationTerminationResponse noShow(
      UUID actor, ReservationId id, String reason, String key) {
    return work.execute(() -> applyDirect(actor, id, "NO_SHOW", reason, key));
  }

  public ReservationTerminationResponse supplierFailure(
      UUID actor, ReservationId id, String reason, String key) {
    return work.execute(() -> applyDirect(actor, id, "SUPPLIER_FAILURE", reason, key));
  }

  public ReservationTerminationResponse requestWithdrawal(
      UUID actor, ReservationId id, String reason, String key) {
    return requestReview(actor, id, reason, key, "WITHDRAWAL");
  }

  public ReservationTerminationResponse requestExternalReview(
      UUID actor, ReservationId id, String reason, String key) {
    return requestReview(actor, id, reason, key, "EXTERNAL_REVIEW");
  }

  private ReservationTerminationResponse requestReview(
      UUID actor, ReservationId id, String reason, String key, String type) {
    return work.execute(
        () -> {
          var repeated = terminations.findByIdempotencyKey(key);
          if (repeated.isPresent()) {
            requireSameOperation(repeated.get(), id, type, reason);
            requireActor(actor, references.reservation(actor, id, false), type);
            return ReservationTerminationResponse.from(repeated.get());
          }
          var current = references.reservation(actor, id, false);
          requireActor(actor, current, type);
          if ("EXTERNAL_REVIEW".equals(type) && !lifecycle.hasFailedDelivery(id, "EXTERNAL_CAUSE"))
            throw new BusinessRuleException("External review requires a recorded failed delivery");
          requireNoRental(id);
          if (lifecycle.hasDelivery(id))
            throw new BusinessRuleException(
                "Administrative review is closed after delivery starts");
          if (current.status() != ReservationStatus.CONFIRMED)
            throw new BusinessRuleException(
                "Only confirmed reservations support administrative review");
          var zero = zero(current);
          var termination =
              ReservationTermination.create(
                  ReservationTerminationId.generate(),
                  id,
                  type,
                  "REQUESTED",
                  new UserId(actor),
                  Instant.now(clock),
                  null,
                  null,
                  reason,
                  zero,
                  zero,
                  zero,
                  key);
          reservations.execute(
              id,
              context -> {
                context.reservation().requestWithdrawalReview();
                return context.reservation();
              });
          return ReservationTerminationResponse.from(terminations.save(termination));
        });
  }

  public ReservationTerminationResponse resolveWithdrawal(
      UUID actor, ReservationTerminationId id, boolean approved) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          var value =
              terminations
                  .findById(id)
                  .orElseThrow(
                      () -> new ReferenceNotFoundApplicationException("Reservation termination"));
          if (!Set.of("WITHDRAWAL", "EXTERNAL_REVIEW").contains(value.type()))
            throw new BusinessRuleException("Termination is not an administrative review");
          var reservation = references.reservation(actor, value.reservationId(), false);
          Money fee = zero(reservation);
          Money refund = zero(reservation);
          Money outstanding = zero(reservation);
          if (approved) {
            refund = paid(reservation);
            reservations.execute(
                value.reservationId(),
                context -> {
                  context.reservation().cancel(clock);
                  return context.reservation();
                });
          } else {
            reservations.execute(
                value.reservationId(),
                context -> {
                  context.reservation().rejectWithdrawal();
                  return context.reservation();
                });
          }
          var resolved =
              value.resolve(
                  approved ? "APPROVED" : "REJECTED",
                  new UserId(actor),
                  Instant.now(clock),
                  fee,
                  refund,
                  outstanding);
          var saved = terminations.save(resolved);
          if (approved) createRefunds(saved, refund);
          return ReservationTerminationResponse.from(saved);
        });
  }

  public List<ReservationTerminationResponse> list(UUID actor) {
    return work.execute(
        () -> {
          access.requireActive(actor);
          return terminations.findByParticipant(actor).stream()
              .map(ReservationTerminationResponse::from)
              .toList();
        });
  }

  private ReservationTerminationResponse applyDirect(
      UUID actor, ReservationId id, String type, String reason, String key) {
    var repeated = terminations.findByIdempotencyKey(key);
    if (repeated.isPresent()) {
      requireSameOperation(repeated.get(), id, type, reason);
      requireActor(actor, references.reservation(actor, id, false), type);
      return ReservationTerminationResponse.from(repeated.get());
    }
    var current = references.reservation(actor, id, false);
    requireActor(actor, current, type);
    if ("SUPPLIER_FAILURE".equals(type)
        && !lifecycle.hasFailedDelivery(id, "SUPPLIER_RESPONSIBILITY"))
      throw new BusinessRuleException("Supplier failure requires a recorded failed delivery");
    if ("SUPPLIER_FAILURE".equals(type) && current.status() != ReservationStatus.CONFIRMED)
      throw new BusinessRuleException("Supplier failure requires a confirmed reservation");
    requireNoRental(id);
    if (lifecycle.hasDelivery(id))
      throw new BusinessRuleException("Commercial termination is closed after delivery starts");
    var policy = requirePolicy(current);
    if ("NO_SHOW".equals(type)
        && Instant.now(clock).isBefore(policy.noShowEligibleAt(current.scheduledFulfillmentAt())))
      throw new BusinessRuleException("No-show grace period has not elapsed");
    BigDecimal rate;
    if ("SUPPLIER_FAILURE".equals(type) || current.status() == ReservationStatus.PENDING)
      rate = BigDecimal.ZERO;
    else if ("NO_SHOW".equals(type)) rate = policy.noShowRate();
    else {
      if (!Instant.now(clock).isBefore(current.scheduledFulfillmentAt()))
        throw new BusinessRuleException(
            "Commercial cancellation is closed after fulfillment starts");
      rate =
          policy.isFreeCancellation(current.scheduledFulfillmentAt(), Instant.now(clock))
              ? BigDecimal.ZERO
              : policy.lateCancellationRate();
    }
    var paid = paid(current);
    var charge = current.total().multiply(rate);
    var refundAmount = moneyDifference(paid, charge);
    var outstanding = moneyDifference(charge, paid);
    var now = Instant.now(clock);
    var termination =
        ReservationTermination.create(
            ReservationTerminationId.generate(),
            id,
            type,
            "APPLIED",
            new UserId(actor),
            now,
            new UserId(actor),
            now,
            reason,
            charge,
            refundAmount,
            outstanding,
            key);
    reservations.execute(
        id,
        context -> {
          if ("NO_SHOW".equals(type)) context.reservation().markNoShow();
          else context.reservation().cancel(clock);
          return context.reservation();
        });
    var saved = terminations.save(termination);
    createRefunds(saved, refundAmount);
    if (outstanding.amount().signum() > 0)
      payments.save(
          Payment.adjustment(
              PaymentId.generate(),
              id,
              outstanding.amount(),
              "TERMINATION",
              saved.id().value(),
              key + ":adjustment",
              clock));
    return ReservationTerminationResponse.from(saved);
  }

  private void createRefunds(ReservationTermination termination, Money target) {
    var remaining = target.amount();
    for (var payment : payments.findByReservation(termination.reservationId())) {
      if (remaining.signum() <= 0) break;
      if (!"CONFIRMED".equals(payment.paymentStatus())) continue;
      var amount = remaining.min(payment.amount());
      var key = termination.idempotencyKey() + ":refund:" + payment.id().value();
      if (refunds.findByIdempotencyKey(key).isEmpty())
        refunds.save(
            Refund.pending(
                RefundId.generate(),
                payment.id(),
                termination.id(),
                new Money(amount, target.currency()),
                key,
                Instant.now(clock)));
      remaining = remaining.subtract(amount);
    }
  }

  private Money paid(Reservation reservation) {
    var total =
        payments.findByReservation(reservation.id()).stream()
            .filter(p -> "CONFIRMED".equals(p.paymentStatus()))
            .map(Payment::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    return new Money(total, reservation.total().currency());
  }

  private Money zero(Reservation r) {
    return new Money(BigDecimal.ZERO, r.total().currency());
  }

  private Money moneyDifference(Money left, Money right) {
    return left.amount().compareTo(right.amount()) > 0
        ? left.subtract(right)
        : new Money(BigDecimal.ZERO, left.currency());
  }

  private com.toolrental.domain.cancellationpolicy.model.aggregate.CancellationPolicy requirePolicy(
      Reservation r) {
    return policies
        .findById(r.cancellationPolicyId())
        .orElseThrow(() -> new ReferenceNotFoundApplicationException("Cancellation policy"));
  }

  private void requireNoRental(ReservationId id) {
    if (rentals.findById(new RentalId(id.value())).isPresent())
      throw new BusinessRuleException("Commercial termination is closed after rental starts");
  }

  private void requireSameOperation(
      ReservationTermination previous, ReservationId reservationId, String type, String reason) {
    if (!previous.reservationId().equals(reservationId)
        || !previous.type().equals(type)
        || !Objects.equals(previous.reason(), reason))
      throw new IdempotencyConflictApplicationException();
  }

  private void requireActor(UUID actor, Reservation reservation, String type) {
    if ("STANDARD".equals(type) || "WITHDRAWAL".equals(type))
      access.requireOwnerOrAdministrator(actor, reservation.customerId().value());
    else access.requireOwnerOrAdministrator(actor, reservation.supplierId().value());
  }
}
