package com.toolrental.application.payment.usecase;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.payment.command.RegisterPaymentCommand;
import com.toolrental.application.payment.dto.PaymentResponse;
import com.toolrental.application.payment.exception.PaymentNotFoundApplicationException;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.payment.model.aggregate.Payment;
import com.toolrental.domain.payment.model.valueobject.PaymentId;
import com.toolrental.domain.payment.port.repository.PaymentRepository;
import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ManagePaymentUseCase {
  private final PaymentRepository repository;
  private final UnitOfWork work;
  private final AccessPolicy access;
  private final WorkflowReferences references;
  private final Clock clock;

  public ManagePaymentUseCase(
      PaymentRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock) {
    this.repository = repository;
    this.work = work;
    this.access = access;
    this.references = references;
    this.clock = clock;
  }

  private Payment load(PaymentId id) {
    return repository.findById(id).orElseThrow(PaymentNotFoundApplicationException::new);
  }

  public PaymentResponse find(UUID actor, PaymentId id) {
    return work.execute(
        () -> {
          var value = load(id);
          references.reservation(actor, value.reservationId(), false);
          return PaymentResponse.from(value);
        });
  }

  public List<PaymentResponse> list(UUID actor) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          return repository.findAll().stream().map(PaymentResponse::from).toList();
        });
  }

  public PaymentResponse register(UUID actor, RegisterPaymentCommand command) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          var reservation = references.reservation(actor, command.reservationId(), false);
          WorkflowReferences.require(
              reservation.status() == ReservationStatus.CONFIRMED, "Reservation must be confirmed");
          var id = PaymentId.generate();
          if (repository.findById(id).isPresent())
            throw new BusinessRuleException("Payment already exists");
          var value =
              Payment.register(
                  id,
                  command.reservationId(),
                  command.processor(),
                  command.externalReference(),
                  command.amount(),
                  "PENDING",
                  Instant.now(clock),
                  null,
                  clock);
          var saved = repository.save(value);
          return PaymentResponse.from(saved);
        });
  }

  public PaymentResponse transition(UUID actor, PaymentId id, String next) {
    return work.execute(
        () -> {
          var value = load(id);
          access.requireAdministrator(actor);
          return PaymentResponse.from(repository.save(value.transition(next, clock)));
        });
  }
}
