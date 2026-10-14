package com.toolrental.domain.reservation.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record ReservaId(UUID value) {
  public ReservaId {
    Objects.requireNonNull(value, "ReservaId");
  }

  public static ReservaId generar() {
    return new ReservaId(UUID.randomUUID());
  }
}
