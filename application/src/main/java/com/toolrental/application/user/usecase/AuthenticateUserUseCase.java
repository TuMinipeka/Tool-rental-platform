package com.toolrental.application.user.usecase;

import com.toolrental.application.user.dto.AuthenticatedSession;
import com.toolrental.application.user.dto.AuthenticationResponse;
import com.toolrental.application.user.exception.AuthenticationFailedApplicationException;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.user.port.AuthenticationTokenProvider;
import com.toolrental.domain.user.port.PasswordHasher;
import com.toolrental.domain.user.port.SecureTokenService;
import com.toolrental.domain.user.port.UserRoleReader;
import com.toolrental.domain.user.port.repository.UserRepository;
import com.toolrental.domain.usersession.model.aggregate.UserSession;
import com.toolrental.domain.usersession.model.valueobject.UserSessionId;
import com.toolrental.domain.usersession.port.repository.UserSessionRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

public final class AuthenticateUserUseCase {
  private final UserRepository users;
  private final PasswordHasher passwords;
  private final UserRoleReader roles;
  private final AuthenticationTokenProvider tokens;
  private final UnitOfWork work;
  private final Clock clock;
  private final UserSessionRepository sessions;
  private final SecureTokenService secrets;
  private final Duration refreshTtl;
  private final String unknownAccountPasswordHash;

  public AuthenticateUserUseCase(
      UserRepository users,
      PasswordHasher passwords,
      UserRoleReader roles,
      AuthenticationTokenProvider tokens,
      UnitOfWork work,
      Clock clock,
      UserSessionRepository sessions,
      SecureTokenService secrets,
      Duration refreshTtl) {
    this.users = users;
    this.passwords = passwords;
    this.roles = roles;
    this.tokens = tokens;
    this.work = work;
    this.clock = clock;
    this.sessions = sessions;
    this.secrets = secrets;
    this.refreshTtl = refreshTtl;
    this.unknownAccountPasswordHash = passwords.hash("UnknownAccountVerificationOnly");
  }

  public AuthenticatedSession execute(String email, String password, String clientLabel) {
    return work.execute(
        () -> {
          var normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
          var found = users.findByEmail(normalizedEmail);
          var matches =
              passwords.matches(
                  password,
                  found.map(value -> value.passwordHash()).orElse(unknownAccountPasswordHash));
          if (found.isEmpty() || !matches || !"ACTIVE".equals(found.get().accountStatus())) {
            throw new AuthenticationFailedApplicationException();
          }
          var user = found.get();
          var assignedRoles = roles.findRoleCodes(user.id());
          var now = Instant.now(clock);
          var secret = secrets.issue();
          var sessionId = UserSessionId.generate();
          sessions.save(
              UserSession.start(
                  sessionId,
                  java.util.UUID.randomUUID(),
                  user.id(),
                  secret.hash(),
                  now,
                  now.plus(refreshTtl),
                  clientLabel));
          var token = tokens.issueAccessToken(user.id(), assignedRoles, sessionId, now);
          return new AuthenticatedSession(
              AuthenticationResponse.from(user, assignedRoles, token), secret.value());
        });
  }
}
