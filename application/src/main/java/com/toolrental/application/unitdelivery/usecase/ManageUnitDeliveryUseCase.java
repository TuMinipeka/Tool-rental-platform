package com.toolrental.application.unitdelivery.usecase;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.unitdelivery.command.RegisterUnitDeliveryCommand;
import com.toolrental.application.unitdelivery.dto.UnitDeliveryResponse;
import com.toolrental.application.unitdelivery.exception.UnitDeliveryNotFoundApplicationException;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import com.toolrental.domain.unitdelivery.model.aggregate.UnitDelivery;
import com.toolrental.domain.unitdelivery.model.valueobject.UnitDeliveryId;
import com.toolrental.domain.unitdelivery.port.repository.UnitDeliveryRepository;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

public final class ManageUnitDeliveryUseCase {
  private final UnitDeliveryRepository repository;
  private final UnitOfWork work;
  private final AccessPolicy access;
  private final WorkflowReferences references;
  private final Clock clock;

  public ManageUnitDeliveryUseCase(
      UnitDeliveryRepository repository,
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

  private UnitDelivery load(UnitDeliveryId id) {
    return repository.findById(id).orElseThrow(UnitDeliveryNotFoundApplicationException::new);
  }

  public UnitDeliveryResponse find(UUID actor, UnitDeliveryId id) {
    return work.execute(
        () -> {
          var value = load(id);
          references.reservation(actor, value.id().reservationId(), false);
          return UnitDeliveryResponse.from(value);
        });
  }

  public List<UnitDeliveryResponse> list(UUID actor) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          return repository.findAll().stream().map(UnitDeliveryResponse::from).toList();
        });
  }

  public UnitDeliveryResponse register(UUID actor, RegisterUnitDeliveryCommand command) {
    return work.execute(
        () -> {
          var reservation = references.reservation(actor, command.id().reservationId(), true);
          WorkflowReferences.require(
              reservation.status() == ReservationStatus.CONFIRMED, "Reservation must be confirmed");
          references.item(reservation, command.id().equipmentUnitId());
          var id = command.id();
          if (repository.findById(id).isPresent())
            throw new BusinessRuleException("UnitDelivery already exists");
          var value =
              UnitDelivery.register(
                  id,
                  command.scheduledAt(),
                  null,
                  command.outboundCondition(),
                  command.addressLine(),
                  command.city(),
                  command.region(),
                  command.country(),
                  reservation.fulfillmentMethod().name(),
                  clock);
          var saved = repository.save(value);
          return UnitDeliveryResponse.from(saved);
        });
  }

  public UnitDeliveryResponse complete(UUID actor, UnitDeliveryId id, String condition) {
    return work.execute(
        () -> {
          var value = load(id);
          var reservation = references.reservation(actor, id.reservationId(), true);
          WorkflowReferences.require(
              reservation.status() == ReservationStatus.CONFIRMED,
              "Reservation must remain confirmed");
          return UnitDeliveryResponse.from(repository.save(value.complete(condition, clock)));
        });
  }

  public UnitDeliveryResponse fail(UUID actor, UnitDeliveryId id, String type, String reason) {
    return work.execute(
        () -> {
          var value = load(id);
          references.reservation(actor, id.reservationId(), true);
          return UnitDeliveryResponse.from(repository.save(value.fail(type, reason, clock)));
        });
  }
}
