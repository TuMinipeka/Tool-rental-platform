package com.toolrental.domain.reservation.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import java.time.Instant;
import java.util.Objects;

public record ReservationRequested(ReservationId id, Instant occurredOn) implements DomainEvent {
  public ReservationRequested {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(occurredOn, "occurredOn must not be null");
  }
}
