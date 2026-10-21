package com.toolrental.application.reservationitem.usecase;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.reservationitem.dto.ReservationItemResponse;
import com.toolrental.application.reservationitem.exception.ReservationItemNotFoundApplicationException;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.reservationitem.model.aggregate.ReservationItem;
import com.toolrental.domain.reservationitem.model.valueobject.ReservationItemId;
import com.toolrental.domain.reservationitem.port.repository.ReservationItemRepository;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

public final class ManageReservationItemUseCase {
  private final ReservationItemRepository repository;
  private final UnitOfWork work;
  private final AccessPolicy access;
  private final WorkflowReferences references;
  private final Clock clock;

  public ManageReservationItemUseCase(
      ReservationItemRepository repository,
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

  private ReservationItem load(ReservationItemId id) {
    return repository.findById(id).orElseThrow(ReservationItemNotFoundApplicationException::new);
  }

  public ReservationItemResponse find(UUID actor, ReservationItemId id) {
    return work.execute(
        () -> {
          var value = load(id);
          references.reservation(actor, value.id().reservationId(), false);
          return ReservationItemResponse.from(value);
        });
  }

  public List<ReservationItemResponse> list(UUID actor) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          return repository.findAll().stream().map(ReservationItemResponse::from).toList();
        });
  }
}
