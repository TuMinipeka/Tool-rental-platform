package com.toolrental.domain.user.model.valueobject;

import java.util.Objects;

public record SecretToken(String value, String hash) {
  public SecretToken {
    Objects.requireNonNull(value, "value is required");
    Objects.requireNonNull(hash, "hash is required");
    if (value.isBlank() || !hash.matches("[0-9a-f]{64}"))
      throw new IllegalArgumentException("Invalid secret token");
  }
}
