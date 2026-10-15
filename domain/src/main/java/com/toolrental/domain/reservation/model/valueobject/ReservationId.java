package com.toolrental.domain.reservation.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record ReservationId(UUID value) {
  public ReservationId {
    Objects.requireNonNull(value, "ReservationId");
  }

  public static ReservationId generate() {
    return new ReservationId(UUID.randomUUID());
  }
}
