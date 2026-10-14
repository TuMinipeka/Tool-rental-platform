package com.toolrental.domain.catalog.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record HerramientaId(UUID value) {
  public HerramientaId {
    Objects.requireNonNull(value, "HerramientaId");
  }

  public static HerramientaId generar() {
    return new HerramientaId(UUID.randomUUID());
  }
}
