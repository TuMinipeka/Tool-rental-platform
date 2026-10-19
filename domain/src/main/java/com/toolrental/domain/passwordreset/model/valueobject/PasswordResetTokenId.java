package com.toolrental.domain.passwordreset.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record PasswordResetTokenId(UUID value) {
  public PasswordResetTokenId {
    Objects.requireNonNull(value, "value is required");
  }

  public static PasswordResetTokenId generate() {
    return new PasswordResetTokenId(UUID.randomUUID());
  }
}
