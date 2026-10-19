package com.toolrental.application.usersession.usecase;

import com.toolrental.application.common.exception.AccessDeniedApplicationException;
import com.toolrental.application.usersession.dto.UserSessionResponse;
import com.toolrental.application.usersession.exception.InvalidSessionApplicationException;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.domain.user.port.SecureTokenService;
import com.toolrental.domain.usersession.model.valueobject.UserSessionId;
import com.toolrental.domain.usersession.port.repository.UserSessionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ManageUserSessionUseCase {
  private final UserSessionRepository sessions;
  private final SecureTokenService secrets;
  private final UnitOfWork work;
  private final Clock clock;

  public ManageUserSessionUseCase(
      UserSessionRepository sessions, SecureTokenService secrets, UnitOfWork work, Clock clock) {
    this.sessions = sessions;
    this.secrets = secrets;
    this.work = work;
    this.clock = clock;
  }

  public void logout(String rawRefreshToken) {
    if (rawRefreshToken == null || rawRefreshToken.isBlank())
      throw new InvalidSessionApplicationException();
    work.execute(
        () -> {
          var session =
              sessions
                  .findByRefreshTokenHash(secrets.hash(rawRefreshToken))
                  .orElseThrow(InvalidSessionApplicationException::new);
          sessions.save(session.revoke("REVOKED", Instant.now(clock)));
          return null;
        });
  }

  public void logoutAll(UUID actor) {
    work.execute(
        () -> {
          sessions.revokeAllForUser(new UserId(actor), Instant.now(clock));
          return null;
        });
  }

  public List<UserSessionResponse> list(UUID actor, UUID currentSessionId) {
    return work.execute(
        () ->
            sessions.findByUser(new UserId(actor)).stream()
                .map(session -> UserSessionResponse.from(session, currentSessionId))
                .toList());
  }

  public void revoke(UUID actor, UserSessionId sessionId) {
    work.execute(
        () -> {
          var session =
              sessions.findById(sessionId).orElseThrow(InvalidSessionApplicationException::new);
          if (!session.userId().value().equals(actor)) throw new AccessDeniedApplicationException();
          sessions.save(session.revoke("REVOKED", Instant.now(clock)));
          return null;
        });
  }
}
