package com.toolrental.application.reservation.usecase;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.reservation.dto.ReservationResponse;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import java.util.UUID;

public final class QueryReservationUseCase {
  private final WorkflowReferences references;
  private final UnitOfWork work;

  public QueryReservationUseCase(WorkflowReferences references, UnitOfWork work) {
    this.references = references;
    this.work = work;
  }

  public ReservationResponse execute(UUID actor, ReservationId id) {
    return work.execute(() -> ReservationResponse.from(references.reservation(actor, id, false)));
  }
}
