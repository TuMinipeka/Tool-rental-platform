package com.toolrental.application.payment.usecase;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.payment.dto.HostedCheckoutResponse;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.payment.port.FictionalPaymentDecision;
import com.toolrental.domain.payment.port.repository.HostedCheckoutRepository;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class DecideFictionalCheckoutUseCase {
  private final WorkflowReferences references;
  private final AccessPolicy access;
  private final HostedCheckoutRepository checkouts;
  private final FictionalPaymentDecision decisions;
  private final ReconcileHostedPaymentUseCase reconcile;
  private final UnitOfWork work;
  private final Clock clock;

  public DecideFictionalCheckoutUseCase(
      WorkflowReferences references,
      AccessPolicy access,
      HostedCheckoutRepository checkouts,
      FictionalPaymentDecision decisions,
      ReconcileHostedPaymentUseCase reconcile,
      UnitOfWork work,
      Clock clock) {
    this.references = references;
    this.access = access;
    this.checkouts = checkouts;
    this.decisions = decisions;
    this.reconcile = reconcile;
    this.work = work;
    this.clock = clock;
  }

  public HostedCheckoutResponse execute(UUID actor, UUID id, String outcome) {
    if (!Set.of("APPROVED", "DECLINED").contains(outcome))
      throw new IllegalArgumentException("Invalid fictional outcome");
    work.execute(
        () -> {
          access.requireRole(actor, "CUSTOMER");
          var checkout =
              checkouts
                  .find(id)
                  .orElseThrow(() -> new IllegalArgumentException("Unknown checkout"));
          if (!checkout.processor().equals("FICTIONAL_GATEWAY"))
            throw new BusinessRuleException("Checkout is not fictional");
          var reservation =
              references.reservation(actor, new ReservationId(checkout.reservationId()), false);
          if (!reservation.customerId().value().equals(actor))
            throw new BusinessRuleException("Only the customer can simulate payment");
          if (decisions.outcome(id) == null
              && (reservation.status() != ReservationStatus.PENDING_PAYMENT
                  || !Instant.now(clock).isBefore(reservation.expiresAt())))
            throw new BusinessRuleException("Reservation is no longer payable");
          decisions.decide(id, outcome);
          return null;
        });
    reconcile.webhook(id.toString());
    return HostedCheckoutResponse.from(checkouts.find(id).orElseThrow());
  }
}
