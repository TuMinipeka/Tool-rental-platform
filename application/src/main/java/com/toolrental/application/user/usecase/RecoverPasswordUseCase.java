package com.toolrental.application.user.usecase;

import com.toolrental.application.user.dto.PasswordRecoveryResponse;
import com.toolrental.application.user.exception.InvalidPasswordResetApplicationException;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.passwordreset.model.aggregate.PasswordResetToken;
import com.toolrental.domain.passwordreset.model.valueobject.PasswordResetTokenId;
import com.toolrental.domain.passwordreset.port.repository.PasswordResetTokenRepository;
import com.toolrental.domain.user.port.PasswordHasher;
import com.toolrental.domain.user.port.PasswordRecoveryNotifier;
import com.toolrental.domain.user.port.SecureTokenService;
import com.toolrental.domain.user.port.repository.UserRepository;
import com.toolrental.domain.usersession.port.repository.UserSessionRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

public final class RecoverPasswordUseCase {
  private final UserRepository users;
  private final PasswordHasher passwords;
  private final PasswordResetTokenRepository resetTokens;
  private final SecureTokenService secrets;
  private final PasswordRecoveryNotifier notifier;
  private final UserSessionRepository sessions;
  private final UnitOfWork work;
  private final Clock clock;
  private final Duration resetTtl;

  public RecoverPasswordUseCase(
      UserRepository users,
      PasswordHasher passwords,
      PasswordResetTokenRepository resetTokens,
      SecureTokenService secrets,
      PasswordRecoveryNotifier notifier,
      UserSessionRepository sessions,
      UnitOfWork work,
      Clock clock,
      Duration resetTtl) {
    this.users = users;
    this.passwords = passwords;
    this.resetTokens = resetTokens;
    this.secrets = secrets;
    this.notifier = notifier;
    this.sessions = sessions;
    this.work = work;
    this.clock = clock;
    this.resetTtl = resetTtl;
  }

  public PasswordRecoveryResponse request(String email) {
    return work.execute(
        () ->
            users
                .findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .filter(user -> "ACTIVE".equals(user.accountStatus()))
                .map(
                    user -> {
                      var now = Instant.now(clock);
                      resetTokens.revokeUnusedForUser(user.id(), now);
                      var secret = secrets.issue();
                      var expiresAt = now.plus(resetTtl);
                      resetTokens.save(
                          PasswordResetToken.issue(
                              PasswordResetTokenId.generate(),
                              user.id(),
                              secret.hash(),
                              now,
                              expiresAt));
                      try {
                        notifier.send(user.email(), secret.value(), expiresAt);
                      } catch (RuntimeException ignored) {
                        // The public response remains generic and does not reveal account
                        // existence.
                      }
                      return new PasswordRecoveryResponse(true);
                    })
                .orElseGet(() -> new PasswordRecoveryResponse(true)));
  }

  public void reset(String token, String newPassword) {
    work.execute(
        () -> {
          try {
            var now = Instant.now(clock);
            var resetToken =
                resetTokens
                    .findByTokenHash(secrets.hash(token))
                    .orElseThrow(InvalidPasswordResetApplicationException::new);
            resetToken = resetToken.consume(now);
            var user =
                users
                    .findById(resetToken.userId())
                    .orElseThrow(InvalidPasswordResetApplicationException::new);
            if (!"ACTIVE".equals(user.accountStatus())) {
              throw new InvalidPasswordResetApplicationException();
            }
            users.save(user.changePassword(passwords.hash(newPassword), clock));
            resetTokens.save(resetToken);
            sessions.revokeAllForUser(user.id(), now);
            return null;
          } catch (InvalidPasswordResetApplicationException exception) {
            throw exception;
          } catch (RuntimeException exception) {
            throw new InvalidPasswordResetApplicationException();
          }
        });
  }
}
