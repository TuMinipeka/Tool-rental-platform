package com.toolrental.domain.catalog.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record ToolId(UUID value) {
  public ToolId {
    Objects.requireNonNull(value, "ToolId");
  }

  public static ToolId generate() {
    return new ToolId(UUID.randomUUID());
  }
}
