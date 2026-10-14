package com.toolrental.domain.catalog.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record UnidadEquipoId(UUID value) {
  public UnidadEquipoId {
    Objects.requireNonNull(value, "UnidadEquipoId");
  }

  public static UnidadEquipoId generar() {
    return new UnidadEquipoId(UUID.randomUUID());
  }
}
