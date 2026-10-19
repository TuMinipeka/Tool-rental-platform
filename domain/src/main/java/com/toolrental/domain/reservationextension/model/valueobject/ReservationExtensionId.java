package com.toolrental.domain.reservationextension.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record ReservationExtensionId(UUID value) {
  public ReservationExtensionId {
    Objects.requireNonNull(value);
  }

  public static ReservationExtensionId generate() {
    return new ReservationExtensionId(UUID.randomUUID());
  }
}
