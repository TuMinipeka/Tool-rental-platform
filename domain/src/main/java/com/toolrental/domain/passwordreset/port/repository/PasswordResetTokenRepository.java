package com.toolrental.domain.passwordreset.port.repository;

import com.toolrental.domain.passwordreset.model.aggregate.PasswordResetToken;
import com.toolrental.domain.user.model.valueobject.UserId;
import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository {
  Optional<PasswordResetToken> findByTokenHash(String tokenHash);

  PasswordResetToken save(PasswordResetToken token);

  void revokeUnusedForUser(UserId userId, Instant revokedAt);
}
