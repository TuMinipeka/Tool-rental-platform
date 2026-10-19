package com.toolrental.application.user.usecase;

import com.toolrental.application.user.dto.PasswordRecoveryResponse;
import com.toolrental.application.user.exception.InvalidPasswordResetApplicationException;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.user.port.AuthenticationTokenProvider;
import com.toolrental.domain.user.port.PasswordHasher;
import com.toolrental.domain.user.port.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

public final class RecoverPasswordUseCase {
  private final UserRepository users;
  private final PasswordHasher passwords;
  private final AuthenticationTokenProvider tokens;
  private final UnitOfWork work;
  private final Clock clock;

  public RecoverPasswordUseCase(
      UserRepository users,
      PasswordHasher passwords,
      AuthenticationTokenProvider tokens,
      UnitOfWork work,
      Clock clock) {
    this.users = users;
    this.passwords = passwords;
    this.tokens = tokens;
    this.work = work;
    this.clock = clock;
  }

  public PasswordRecoveryResponse request(String email) {
    return work.execute(
        () ->
            users
                .findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .filter(user -> "ACTIVE".equals(user.accountStatus()))
                .map(
                    user -> {
                      var token =
                          tokens.issuePasswordResetToken(
                              user.id(), user.passwordHash(), Instant.now(clock));
                      return new PasswordRecoveryResponse(true, token.value(), token.expiresAt());
                    })
                .orElseGet(() -> new PasswordRecoveryResponse(true, null, null)));
  }

  public void reset(String token, String newPassword) {
    work.execute(
        () -> {
          try {
            var subject = tokens.verifyPasswordResetToken(token);
            var user =
                users
                    .findById(subject.userId())
                    .orElseThrow(InvalidPasswordResetApplicationException::new);
            if (!"ACTIVE".equals(user.accountStatus())
                || !tokens.matchesCurrentPassword(subject, user.passwordHash())) {
              throw new InvalidPasswordResetApplicationException();
            }
            users.save(user.changePassword(passwords.hash(newPassword), clock));
            return null;
          } catch (InvalidPasswordResetApplicationException exception) {
            throw exception;
          } catch (RuntimeException exception) {
            throw new InvalidPasswordResetApplicationException();
          }
        });
  }
}
