package com.toolrental.application.unitreturn.usecase;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.unitreturn.command.RegisterUnitReturnCommand;
import com.toolrental.application.unitreturn.dto.UnitReturnResponse;
import com.toolrental.application.unitreturn.exception.UnitReturnNotFoundApplicationException;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.payment.model.aggregate.Payment;
import com.toolrental.domain.payment.model.valueobject.PaymentId;
import com.toolrental.domain.payment.port.repository.PaymentRepository;
import com.toolrental.domain.rental.model.valueobject.RentalId;
import com.toolrental.domain.rental.port.repository.RentalRepository;
import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import com.toolrental.domain.unitdelivery.model.valueobject.UnitDeliveryId;
import com.toolrental.domain.unitdelivery.port.repository.UnitDeliveryRepository;
import com.toolrental.domain.unitreturn.model.aggregate.UnitReturn;
import com.toolrental.domain.unitreturn.model.valueobject.UnitReturnId;
import com.toolrental.domain.unitreturn.port.repository.UnitReturnRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public final class ManageUnitReturnUseCase {
  private final UnitReturnRepository repository;
  private final UnitOfWork work;
  private final AccessPolicy access;
  private final WorkflowReferences references;
  private final Clock clock;
  private final UnitDeliveryRepository deliveries;
  private final RentalRepository rentals;
  private final PaymentRepository payments;

  public ManageUnitReturnUseCase(
      UnitReturnRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      UnitDeliveryRepository deliveries,
      RentalRepository rentals,
      PaymentRepository payments) {
    this.repository = repository;
    this.work = work;
    this.access = access;
    this.references = references;
    this.clock = clock;
    this.deliveries = deliveries;
    this.rentals = rentals;
    this.payments = payments;
  }

  private UnitReturn load(UnitReturnId id) {
    return repository.findById(id).orElseThrow(UnitReturnNotFoundApplicationException::new);
  }

  public UnitReturnResponse find(UUID actor, UnitReturnId id) {
    return work.execute(
        () -> {
          var value = load(id);
          references.reservation(actor, value.id().reservationId(), false);
          return UnitReturnResponse.from(value);
        });
  }

  public List<UnitReturnResponse> list(UUID actor) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          return repository.findAll().stream().map(UnitReturnResponse::from).toList();
        });
  }

  public UnitReturnResponse register(UUID actor, RegisterUnitReturnCommand command) {
    return work.execute(
        () -> {
          var reservation = references.reservation(actor, command.id().reservationId(), true);
          WorkflowReferences.require(
              reservation.status() == ReservationStatus.CONFIRMED, "Reservation must be confirmed");
          references.item(reservation, command.id().equipmentUnitId());
          var rental =
              rentals
                  .findById(new RentalId(reservation.id().value()))
                  .orElseThrow(
                      () ->
                          new com.toolrental.application.common.exception
                              .ReferenceNotFoundApplicationException("Rental"));
          WorkflowReferences.require(
              !"CANCELLED".equals(rental.rentalStatus()), "Rental is cancelled");
          var delivery =
              deliveries
                  .findById(
                      new UnitDeliveryId(
                          command.id().reservationId(), command.id().equipmentUnitId()))
                  .orElseThrow(
                      () ->
                          new com.toolrental.application.common.exception
                              .ReferenceNotFoundApplicationException("Delivery"));
          WorkflowReferences.require(
              delivery.deliveredAt() != null
                  && !Instant.now(clock).isBefore(delivery.deliveredAt()),
              "Delivery must be completed before return");
          var id = command.id();
          if (repository.findById(id).isPresent())
            throw new BusinessRuleException("UnitReturn already exists");
          var now = Instant.now(clock);
          var receivedDate = LocalDate.ofInstant(now, ZoneId.of("America/Bogota"));
          var lateDays =
              receivedDate.isAfter(reservation.effectiveReturnDate())
                  ? (int) ChronoUnit.DAYS.between(reservation.effectiveReturnDate(), receivedDate)
                  : 0;
          var dailyRate =
              reservation.items().stream()
                  .filter(item -> item.equipmentUnitId().equals(command.id().equipmentUnitId()))
                  .findFirst()
                  .orElseThrow()
                  .dailyRate();
          var lateFee =
              lateDays == 0
                  ? new Money(BigDecimal.ZERO, dailyRate.currency())
                  : dailyRate.multiply(lateDays);
          var value =
              UnitReturn.register(
                  id, now, command.inboundCondition(), command.notes(), lateDays, lateFee, clock);
          var saved = repository.save(value);
          if (lateFee.amount().signum() > 0)
            payments.save(
                Payment.adjustment(
                    PaymentId.generate(),
                    reservation.id(),
                    lateFee.amount(),
                    "LATE_RETURN",
                    command.id().equipmentUnitId().value(),
                    "late-return:"
                        + reservation.id().value()
                        + ":"
                        + command.id().equipmentUnitId().value(),
                    clock));
          return UnitReturnResponse.from(saved);
        });
  }
}
