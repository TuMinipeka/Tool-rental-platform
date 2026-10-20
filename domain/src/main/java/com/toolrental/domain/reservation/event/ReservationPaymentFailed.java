package com.toolrental.domain.reservation.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import java.time.Instant;
import java.util.Objects;

public record ReservationPaymentFailed(ReservationId aggregateId, Instant occurredOn)
    implements DomainEvent {
  public ReservationPaymentFailed {
    Objects.requireNonNull(aggregateId);
    Objects.requireNonNull(occurredOn);
  }
}
