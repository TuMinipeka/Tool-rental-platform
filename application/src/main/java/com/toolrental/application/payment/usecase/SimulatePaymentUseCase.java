package com.toolrental.application.payment.usecase;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.payment.command.SimulatePaymentCommand;
import com.toolrental.application.payment.dto.PaymentResponse;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.payment.model.aggregate.Payment;
import com.toolrental.domain.payment.model.valueobject.PaymentId;
import com.toolrental.domain.payment.port.repository.PaymentRepository;
import com.toolrental.domain.rental.model.valueobject.RentalId;
import com.toolrental.domain.rental.port.repository.RentalRepository;
import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.util.Set;
import java.util.UUID;

public final class SimulatePaymentUseCase {
  private final PaymentRepository payments;
  private final RentalRepository rentals;
  private final WorkflowReferences references;
  private final AccessPolicy access;
  private final UnitOfWork work;
  private final Clock clock;

  public SimulatePaymentUseCase(
      PaymentRepository payments,
      RentalRepository rentals,
      WorkflowReferences references,
      AccessPolicy access,
      UnitOfWork work,
      Clock clock) {
    this.payments = payments;
    this.rentals = rentals;
    this.references = references;
    this.access = access;
    this.work = work;
    this.clock = clock;
  }

  public PaymentResponse execute(UUID actor, SimulatePaymentCommand command) {
    return work.execute(
        () -> {
          access.requireRole(actor, "CUSTOMER");
          var previous = payments.findByIdempotencyKey(command.idempotencyKey());
          if (previous.isPresent()) {
            if (!previous.get().reservationId().equals(command.reservationId()))
              throw new BusinessRuleException("Idempotency key belongs to another reservation");
            references.reservation(actor, previous.get().reservationId(), false);
            return PaymentResponse.from(previous.get());
          }

          var reservation = references.reservation(actor, command.reservationId(), false);
          if (!reservation.customerId().value().equals(actor))
            throw new BusinessRuleException("Only the customer can pay a reservation");
          if (reservation.status() != ReservationStatus.CONFIRMED)
            throw new BusinessRuleException("Reservation must be confirmed");

          validateStage(command.paymentStage(), reservation.id());
          validateMethod(command);
          var status =
              switch (command.simulationOutcome()) {
                case "APPROVE" -> "CONFIRMED";
                case "DECLINE" -> "FAILED";
                default -> throw new BusinessRuleException("Invalid simulation outcome");
              };
          var amount = reservation.total().amount().multiply(new BigDecimal("0.50"));
          var method = command.paymentMethod();
          var processor = "PAYPAL".equals(method) ? "SIMULATED_PAYPAL" : "SIMULATED_CARD";
          var now = Instant.now(clock);
          var payment =
              Payment.simulate(
                  PaymentId.generate(),
                  reservation.id(),
                  processor,
                  "sim_" + UUID.randomUUID(),
                  amount,
                  status,
                  now,
                  method,
                  command.paymentStage(),
                  command.idempotencyKey(),
                  command.cardNetwork(),
                  command.cardLastFour(),
                  command.cardExpiryMonth(),
                  command.cardExpiryYear(),
                  clock);
          return PaymentResponse.from(payments.save(payment));
        });
  }

  private void validateStage(
      String stage,
      com.toolrental.domain.reservation.model.valueobject.ReservationId reservationId) {
    if (!Set.of("FIRST_HALF", "FINAL_HALF").contains(stage))
      throw new BusinessRuleException("Invalid payment stage");
    if (payments.hasConfirmedStage(reservationId, stage))
      throw new BusinessRuleException("Payment stage is already confirmed");
    if ("FINAL_HALF".equals(stage)) {
      if (!payments.hasConfirmedStage(reservationId, "FIRST_HALF"))
        throw new BusinessRuleException("First payment must be confirmed");
      var rental =
          rentals
              .findById(new RentalId(reservationId.value()))
              .orElseThrow(() -> new BusinessRuleException("Rental does not exist"));
      if (!"COMPLETED".equals(rental.rentalStatus()))
        throw new BusinessRuleException("Rental must be completed for the final payment");
    }
  }

  private void validateMethod(SimulatePaymentCommand command) {
    if (!Set.of("CREDIT_CARD", "DEBIT_CARD", "PAYPAL").contains(command.paymentMethod()))
      throw new BusinessRuleException("Invalid payment method");
    if (Set.of("CREDIT_CARD", "DEBIT_CARD").contains(command.paymentMethod())) {
      if (command.cardExpiryYear() == null || command.cardExpiryMonth() == null)
        throw new BusinessRuleException("Card expiry is required");
      var expiry = YearMonth.of(command.cardExpiryYear(), command.cardExpiryMonth());
      if (expiry.isBefore(YearMonth.now(clock)))
        throw new BusinessRuleException("Simulated card is expired");
    }
  }
}
