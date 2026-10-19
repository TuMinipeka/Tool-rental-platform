package com.toolrental.domain.user.model.valueobject;

import java.time.Instant;
import java.util.Objects;

public record IssuedToken(String value, Instant expiresAt) {
  public IssuedToken {
    Objects.requireNonNull(value, "value is required");
    Objects.requireNonNull(expiresAt, "expiresAt is required");
    if (value.isBlank()) throw new IllegalArgumentException("value cannot be blank");
  }
}
