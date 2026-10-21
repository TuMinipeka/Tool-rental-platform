package com.toolrental.application.payment.usecase;

import com.toolrental.application.common.exception.IdempotencyConflictApplicationException;
import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.payment.command.CreateSandboxCheckoutCommand;
import com.toolrental.application.payment.dto.PaymentResponse;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.payment.model.aggregate.Payment;
import com.toolrental.domain.payment.model.valueobject.PaymentId;
import com.toolrental.domain.payment.port.repository.PaymentRepository;
import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class CreateSandboxCheckoutUseCase {
  private final PaymentRepository payments;
  private final WorkflowReferences references;
  private final AccessPolicy access;
  private final UnitOfWork work;
  private final Clock clock;

  public CreateSandboxCheckoutUseCase(
      PaymentRepository payments,
      WorkflowReferences references,
      AccessPolicy access,
      UnitOfWork work,
      Clock clock) {
    this.payments = payments;
    this.references = references;
    this.access = access;
    this.work = work;
    this.clock = clock;
  }

  public PaymentResponse execute(UUID actor, CreateSandboxCheckoutCommand command) {
    return work.execute(
        () -> {
          access.requireRole(actor, "CUSTOMER");
          var previous = payments.findByIdempotencyKey(command.idempotencyKey());
          if (previous.isPresent()) {
            var stored = previous.get();
            if (!stored.reservationId().equals(command.reservationId())
                || !stored.paymentMethod().equals(command.paymentMethod())
                || !Objects.equals(stored.cardNetwork(), command.cardNetwork())
                || !Objects.equals(stored.cardLastFour(), command.cardLastFour())
                || !Objects.equals(stored.cardExpiryMonth(), command.cardExpiryMonth())
                || !Objects.equals(stored.cardExpiryYear(), command.cardExpiryYear())) {
              throw new IdempotencyConflictApplicationException();
            }
            references.reservation(actor, stored.reservationId(), false);
            return PaymentResponse.from(stored);
          }

          var reservation = references.reservation(actor, command.reservationId(), false);
          if (!reservation.customerId().value().equals(actor))
            throw new BusinessRuleException("Only the customer can pay a reservation");
          if (reservation.status() != ReservationStatus.PENDING_PAYMENT)
            throw new BusinessRuleException("Reservation is not awaiting payment");
          if (!Instant.now(clock).isBefore(reservation.expiresAt()))
            throw new BusinessRuleException("Payment hold has expired");
          validateMethod(command);

          var payment =
              Payment.sandboxCheckout(
                  PaymentId.generate(),
                  reservation.id(),
                  "sandbox_" + UUID.randomUUID(),
                  reservation.total().amount(),
                  Instant.now(clock),
                  command.paymentMethod(),
                  command.idempotencyKey(),
                  command.cardNetwork(),
                  command.cardLastFour(),
                  command.cardExpiryMonth(),
                  command.cardExpiryYear(),
                  clock);
          return PaymentResponse.from(payments.save(payment));
        });
  }

  private void validateMethod(CreateSandboxCheckoutCommand command) {
    if (!Set.of("CREDIT_CARD", "DEBIT_CARD", "PAYPAL").contains(command.paymentMethod()))
      throw new BusinessRuleException("Invalid payment method");
    if (Set.of("CREDIT_CARD", "DEBIT_CARD").contains(command.paymentMethod())) {
      if (command.cardExpiryYear() == null || command.cardExpiryMonth() == null)
        throw new BusinessRuleException("Card expiry is required");
      if (YearMonth.of(command.cardExpiryYear(), command.cardExpiryMonth())
          .isBefore(YearMonth.now(clock)))
        throw new BusinessRuleException("Sandbox card is expired");
    }
  }
}
