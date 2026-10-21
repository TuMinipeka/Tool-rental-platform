package com.toolrental.application.usersession.usecase;

import com.toolrental.application.user.dto.AuthenticatedSession;
import com.toolrental.application.user.dto.AuthenticationResponse;
import com.toolrental.application.usersession.exception.InvalidSessionApplicationException;
import com.toolrental.application.usersession.exception.RefreshTokenReuseApplicationException;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.user.port.AuthenticationTokenProvider;
import com.toolrental.domain.user.port.SecureTokenService;
import com.toolrental.domain.user.port.UserRoleReader;
import com.toolrental.domain.user.port.repository.UserRepository;
import com.toolrental.domain.usersession.model.valueobject.UserSessionId;
import com.toolrental.domain.usersession.port.repository.UserSessionRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class RefreshSessionUseCase {
  private final UserSessionRepository sessions;
  private final UserRepository users;
  private final UserRoleReader roles;
  private final SecureTokenService secrets;
  private final AuthenticationTokenProvider accessTokens;
  private final UnitOfWork work;
  private final Clock clock;
  private final Duration refreshTtl;
  private final Duration reuseGrace;

  public RefreshSessionUseCase(
      UserSessionRepository sessions,
      UserRepository users,
      UserRoleReader roles,
      SecureTokenService secrets,
      AuthenticationTokenProvider accessTokens,
      UnitOfWork work,
      Clock clock,
      Duration refreshTtl,
      Duration reuseGrace) {
    this.sessions = sessions;
    this.users = users;
    this.roles = roles;
    this.secrets = secrets;
    this.accessTokens = accessTokens;
    this.work = work;
    this.clock = clock;
    this.refreshTtl = refreshTtl;
    this.reuseGrace = reuseGrace;
  }

  public AuthenticatedSession execute(String rawRefreshToken) {
    if (rawRefreshToken == null || rawRefreshToken.isBlank())
      throw new InvalidSessionApplicationException();
    try {
      return work.execute(
          () -> {
            var now = Instant.now(clock);
            var session =
                sessions
                    .findByRefreshTokenHash(secrets.hash(rawRefreshToken))
                    .orElseThrow(InvalidSessionApplicationException::new);
            if ("ROTATED".equals(session.status())) {
              if (session.isRotatedBeyondGrace(now, reuseGrace)) {
                throw new ReusedFamily(session.familyId());
              }
              throw new InvalidSessionApplicationException();
            }
            try {
              session.requireActive(now);
            } catch (RuntimeException exception) {
              throw new InvalidSessionApplicationException();
            }
            var user =
                users
                    .findById(session.userId())
                    .orElseThrow(InvalidSessionApplicationException::new);
            if (!"ACTIVE".equals(user.accountStatus()))
              throw new InvalidSessionApplicationException();
            var nextSecret = secrets.issue();
            var nextId = UserSessionId.generate();
            var rotation = session.rotate(nextId, nextSecret.hash(), now, now.plus(refreshTtl));
            sessions.saveRotation(rotation.rotated(), rotation.successor());
            var assignedRoles = roles.findRoleCodes(user.id());
            var access = accessTokens.issueAccessToken(user.id(), assignedRoles, nextId, now);
            return new AuthenticatedSession(
                AuthenticationResponse.from(user, assignedRoles, access), nextSecret.value());
          });
    } catch (ReusedFamily reused) {
      work.execute(
          () -> {
            sessions.revokeFamily(reused.familyId, Instant.now(clock), "COMPROMISED");
            return null;
          });
      throw new RefreshTokenReuseApplicationException();
    }
  }

  private static final class ReusedFamily extends RuntimeException {
    private final UUID familyId;

    private ReusedFamily(UUID familyId) {
      this.familyId = familyId;
    }
  }
}
