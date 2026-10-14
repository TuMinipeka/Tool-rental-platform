package com.toolrental.domain.reservation.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.reservation.model.valueobject.ReservaId;
import java.time.Instant;
import java.util.Objects;

public record ReservaConfirmada(ReservaId reservaId, Instant occurredOn) implements DomainEvent {
  public ReservaConfirmada {
    Objects.requireNonNull(reservaId, "id must not be null");
    Objects.requireNonNull(occurredOn, "occurredOn must not be null");
  }
}
