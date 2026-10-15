package com.toolrental.domain.rental.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record RentalId(UUID value) {
  public RentalId {
    Objects.requireNonNull(value);
  }

  public static RentalId generate() {
    return new RentalId(UUID.randomUUID());
  }
}
