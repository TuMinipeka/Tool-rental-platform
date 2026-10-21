package com.toolrental.domain.supplier.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record SupplierId(UUID value) {
  public SupplierId {
    Objects.requireNonNull(value, "SupplierId");
  }

  public static SupplierId generate() {
    return new SupplierId(UUID.randomUUID());
  }
}
