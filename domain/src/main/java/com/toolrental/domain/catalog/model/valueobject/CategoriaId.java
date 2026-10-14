package com.toolrental.domain.catalog.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record CategoriaId(UUID value) {
  public CategoriaId {
    Objects.requireNonNull(value, "CategoriaId");
  }

  public static CategoriaId generar() {
    return new CategoriaId(UUID.randomUUID());
  }
}
