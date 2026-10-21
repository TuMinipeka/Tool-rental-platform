package com.toolrental.domain.user.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record UserId(UUID value) {
  public UserId {
    Objects.requireNonNull(value);
  }

  public static UserId generate() {
    return new UserId(UUID.randomUUID());
  }
}
