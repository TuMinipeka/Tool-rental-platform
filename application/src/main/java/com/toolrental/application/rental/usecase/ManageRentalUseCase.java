package com.toolrental.application.rental.usecase;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.rental.command.RegisterRentalCommand;
import com.toolrental.application.rental.dto.RentalResponse;
import com.toolrental.application.rental.exception.RentalNotFoundApplicationException;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.rental.model.aggregate.Rental;
import com.toolrental.domain.rental.model.valueobject.RentalId;
import com.toolrental.domain.rental.port.repository.RentalRepository;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ManageRentalUseCase {
  private final RentalRepository repository;
  private final UnitOfWork work;
  private final AccessPolicy access;
  private final WorkflowReferences references;
  private final Clock clock;
  private final com.toolrental.domain.rental.port.RentalLifecycle lifecycle;

  public ManageRentalUseCase(
      RentalRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      com.toolrental.domain.rental.port.RentalLifecycle lifecycle) {
    this.repository = repository;
    this.work = work;
    this.access = access;
    this.references = references;
    this.clock = clock;
    this.lifecycle = lifecycle;
  }

  private Rental load(RentalId id) {
    return repository.findById(id).orElseThrow(RentalNotFoundApplicationException::new);
  }

  public RentalResponse find(UUID actor, RentalId id) {
    return work.execute(
        () -> {
          var value = load(id);
          references.reservation(actor, new ReservationId(value.id().value()), false);
          return RentalResponse.from(value);
        });
  }

  public List<RentalResponse> list(UUID actor) {
    return work.execute(
        () -> {
          access.requireActive(actor);
          var values =
              access.isAdministrator(actor)
                  ? repository.findAll()
                  : repository.findByParticipant(actor);
          return values.stream().map(RentalResponse::from).toList();
        });
  }

  public RentalResponse register(UUID actor, RegisterRentalCommand command) {
    return work.execute(
        () -> {
          var reservation = references.reservation(actor, command.reservationId(), true);
          WorkflowReferences.require(
              reservation.status() == ReservationStatus.CONFIRMED, "Reservation must be confirmed");
          WorkflowReferences.require(
              lifecycle.allUnitsDelivered(reservation.id()),
              "All units must be delivered or collected before rental starts");
          var id = new RentalId(command.reservationId().value());
          if (repository.findById(id).isPresent())
            throw new BusinessRuleException("Rental already exists");
          var value = Rental.register(id, Instant.now(clock), "ACTIVE", clock);
          var saved = repository.save(value);
          return RentalResponse.from(saved);
        });
  }

  public RentalResponse transition(UUID actor, RentalId id, String next) {
    return work.execute(
        () -> {
          var value = load(id);
          references.reservation(actor, new ReservationId(value.id().value()), true);
          if ("COMPLETED".equals(next))
            WorkflowReferences.require(
                lifecycle.allUnitsReturned(new ReservationId(id.value())),
                "All units must be returned");
          if ("CANCELLED".equals(next))
            WorkflowReferences.require(
                !lifecycle.hasDelivery(new ReservationId(id.value())),
                "Delivered rental cannot be cancelled");
          return RentalResponse.from(repository.save(value.transition(next, clock)));
        });
  }
}
