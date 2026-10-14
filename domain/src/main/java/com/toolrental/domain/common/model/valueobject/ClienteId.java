package com.toolrental.domain.common.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record ClienteId(UUID value) {
  public ClienteId {
    Objects.requireNonNull(value, "ClienteId");
  }

  public static ClienteId generar() {
    return new ClienteId(UUID.randomUUID());
  }
}
