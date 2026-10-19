package com.toolrental.domain.usersession.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record UserSessionId(UUID value) {
  public UserSessionId {
    Objects.requireNonNull(value, "value is required");
  }

  public static UserSessionId generate() {
    return new UserSessionId(UUID.randomUUID());
  }
}
