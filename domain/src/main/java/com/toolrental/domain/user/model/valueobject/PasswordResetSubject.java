package com.toolrental.domain.user.model.valueobject;

import java.util.Objects;

public record PasswordResetSubject(UserId userId, String passwordFingerprint) {
  public PasswordResetSubject {
    Objects.requireNonNull(userId, "userId is required");
    Objects.requireNonNull(passwordFingerprint, "passwordFingerprint is required");
  }
}
