package com.toolrental.domain.catalog.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record CategoryId(UUID value) {
  public CategoryId {
    Objects.requireNonNull(value, "CategoryId");
  }

  public static CategoryId generate() {
    return new CategoryId(UUID.randomUUID());
  }
}
