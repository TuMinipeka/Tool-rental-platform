package com.toolrental.application.user.dto;

import java.util.Objects;

public record AuthenticatedSession(AuthenticationResponse authentication, String refreshToken) {
  public AuthenticatedSession {
    Objects.requireNonNull(authentication);
    if (refreshToken == null || refreshToken.isBlank())
      throw new IllegalArgumentException("refreshToken is required");
  }
}
