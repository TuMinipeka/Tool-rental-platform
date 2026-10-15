package com.toolrental.application.reservation.usecase;

import com.toolrental.application.reservation.dto.ReservationResponse;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.valueobject.CustomerId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservation.port.repository.AtomicReservationManagement;
import java.time.Clock;
import java.util.Objects;

public final class CancelReservationUseCase {
  private final AtomicReservationManagement management;
  private final Clock clock;

  public CancelReservationUseCase(AtomicReservationManagement management, Clock clock) {
    this.management = Objects.requireNonNull(management);
    this.clock = Objects.requireNonNull(clock);
  }

  public ReservationResponse execute(ReservationId id, CustomerId actor) {
    Objects.requireNonNull(actor);
    return ReservationResponse.from(
        management.execute(
            Objects.requireNonNull(id),
            context -> {
              var r = context.reservation();
              if (!r.customerId().equals(actor) || !context.isCustomerActive(actor))
                throw new BusinessRuleException("Customer is not authorized");
              r.cancel(clock);
              return r;
            }));
  }
}
