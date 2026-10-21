package com.toolrental.application.reservation.usecase;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.reservation.dto.ReservationResponse;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.customer.model.valueobject.CustomerId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservation.port.repository.ReservationRepository;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public final class QueryReservationUseCase {
  private final WorkflowReferences references;
  private final UnitOfWork work;
  private final ReservationRepository reservations;
  private final AccessPolicy access;

  public QueryReservationUseCase(
      WorkflowReferences references,
      UnitOfWork work,
      ReservationRepository reservations,
      AccessPolicy access) {
    this.references = references;
    this.work = work;
    this.reservations = reservations;
    this.access = access;
  }

  public ReservationResponse execute(UUID actor, ReservationId id) {
    return work.execute(() -> ReservationResponse.from(references.reservation(actor, id, false)));
  }

  public List<ReservationResponse> list(UUID actor) {
    return work.execute(
        () -> {
          access.requireActive(actor);
          var visible = new LinkedHashMap<ReservationId, ReservationResponse>();
          reservations
              .findByCustomer(new CustomerId(actor))
              .forEach(value -> visible.put(value.id(), ReservationResponse.from(value)));
          reservations
              .findBySupplier(new SupplierId(actor))
              .forEach(value -> visible.put(value.id(), ReservationResponse.from(value)));
          return List.copyOf(visible.values());
        });
  }
}
