package com.toolrental.domain.common.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record ProveedorId(UUID value) {
  public ProveedorId {
    Objects.requireNonNull(value, "ProveedorId");
  }

  public static ProveedorId generar() {
    return new ProveedorId(UUID.randomUUID());
  }
}
