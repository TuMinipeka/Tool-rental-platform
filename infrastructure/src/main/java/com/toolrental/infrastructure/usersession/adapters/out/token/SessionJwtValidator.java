package com.toolrental.infrastructure.usersession.adapters.out.token;

import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.domain.usersession.model.valueobject.UserSessionId;
import com.toolrental.domain.usersession.port.repository.UserSessionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public final class SessionJwtValidator implements OAuth2TokenValidator<Jwt> {
  private static final OAuth2Error INVALID_SESSION =
      new OAuth2Error("invalid_token", "Access session is inactive", null);

  private final UserSessionRepository sessions;
  private final Clock clock;

  public SessionJwtValidator(UserSessionRepository sessions, Clock clock) {
    this.sessions = sessions;
    this.clock = clock;
  }

  public OAuth2TokenValidatorResult validate(Jwt jwt) {
    try {
      var sessionId = new UserSessionId(UUID.fromString(jwt.getClaimAsString("sid")));
      var userId = new UserId(UUID.fromString(jwt.getSubject()));
      return sessions.isActive(sessionId, userId, Instant.now(clock))
          ? OAuth2TokenValidatorResult.success()
          : OAuth2TokenValidatorResult.failure(INVALID_SESSION);
    } catch (RuntimeException exception) {
      return OAuth2TokenValidatorResult.failure(INVALID_SESSION);
    }
  }
}
