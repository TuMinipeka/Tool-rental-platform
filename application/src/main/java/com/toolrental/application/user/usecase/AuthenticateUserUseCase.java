package com.toolrental.application.user.usecase;

import com.toolrental.application.user.dto.AuthenticationResponse;
import com.toolrental.application.user.exception.AuthenticationFailedApplicationException;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.user.port.AuthenticationTokenProvider;
import com.toolrental.domain.user.port.PasswordHasher;
import com.toolrental.domain.user.port.UserRoleReader;
import com.toolrental.domain.user.port.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

public final class AuthenticateUserUseCase {
  private final UserRepository users;
  private final PasswordHasher passwords;
  private final UserRoleReader roles;
  private final AuthenticationTokenProvider tokens;
  private final UnitOfWork work;
  private final Clock clock;

  public AuthenticateUserUseCase(
      UserRepository users,
      PasswordHasher passwords,
      UserRoleReader roles,
      AuthenticationTokenProvider tokens,
      UnitOfWork work,
      Clock clock) {
    this.users = users;
    this.passwords = passwords;
    this.roles = roles;
    this.tokens = tokens;
    this.work = work;
    this.clock = clock;
  }

  public AuthenticationResponse execute(String email, String password) {
    return work.execute(
        () -> {
          var normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
          var user =
              users
                  .findByEmail(normalizedEmail)
                  .orElseThrow(AuthenticationFailedApplicationException::new);
          if (!"ACTIVE".equals(user.accountStatus())
              || !passwords.matches(password, user.passwordHash())) {
            throw new AuthenticationFailedApplicationException();
          }
          var assignedRoles = roles.findRoleCodes(user.id());
          var token = tokens.issueAccessToken(user.id(), assignedRoles, Instant.now(clock));
          return AuthenticationResponse.from(user, assignedRoles, token);
        });
  }
}
