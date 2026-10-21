package com.toolrental.application.reservation.usecase;

import com.toolrental.application.reservation.dto.ReservationResponse;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservation.port.repository.AtomicReservationManagement;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import java.time.Clock;
import java.util.Objects;

public final class RejectReservationUseCase {
  private final AtomicReservationManagement management;
  private final Clock clock;

  public RejectReservationUseCase(AtomicReservationManagement management, Clock clock) {
    this.management = Objects.requireNonNull(management);
    this.clock = Objects.requireNonNull(clock);
  }

  public ReservationResponse execute(ReservationId id, SupplierId actor) {
    Objects.requireNonNull(actor);
    return ReservationResponse.from(
        management.execute(
            Objects.requireNonNull(id),
            context -> {
              var r = context.reservation();
              if (!r.supplierId().equals(actor) || !context.isSupplierActive(actor))
                throw new com.toolrental.application.common.exception
                    .AccessDeniedApplicationException();
              r.reject(clock);
              return r;
            }));
  }
}
