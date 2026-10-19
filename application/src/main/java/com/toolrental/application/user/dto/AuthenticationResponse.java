package com.toolrental.application.user.dto;

import com.toolrental.domain.user.model.aggregate.User;
import com.toolrental.domain.user.model.valueobject.IssuedToken;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AuthenticationResponse(
    UUID userId,
    String email,
    String displayName,
    Set<String> roles,
    String accessToken,
    String tokenType,
    Instant expiresAt) {
  public AuthenticationResponse {
    roles = Set.copyOf(roles);
  }

  public static AuthenticationResponse from(User user, Set<String> roles, IssuedToken token) {
    return new AuthenticationResponse(
        user.id().value(),
        user.email(),
        user.displayName(),
        roles,
        token.value(),
        "Bearer",
        token.expiresAt());
  }
}
