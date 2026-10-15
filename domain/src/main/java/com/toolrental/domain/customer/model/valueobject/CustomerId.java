package com.toolrental.domain.customer.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record CustomerId(UUID value) {
  public CustomerId {
    Objects.requireNonNull(value, "CustomerId");
  }

  public static CustomerId generate() {
    return new CustomerId(UUID.randomUUID());
  }
}
