package com.toolrental.domain.reservationtermination.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record ReservationTerminationId(UUID value) {
  public ReservationTerminationId {
    Objects.requireNonNull(value);
  }

  public static ReservationTerminationId generate() {
    return new ReservationTerminationId(UUID.randomUUID());
  }
}
