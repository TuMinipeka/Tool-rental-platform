package com.toolrental.domain.passwordreset.model.aggregate;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.passwordreset.event.PasswordResetTokenChanged;
import com.toolrental.domain.passwordreset.model.valueobject.PasswordResetTokenId;
import com.toolrental.domain.user.model.valueobject.UserId;
import java.time.Instant;
import java.util.Objects;

public final class PasswordResetToken extends AggregateRoot {
  private final PasswordResetTokenId id;
  private final UserId userId;
  private final String tokenHash;
  private final Instant createdAt;
  private final Instant expiresAt;
  private final Instant usedAt;
  private final Instant revokedAt;

  private PasswordResetToken(
      PasswordResetTokenId id,
      UserId userId,
      String tokenHash,
      Instant createdAt,
      Instant expiresAt,
      Instant usedAt,
      Instant revokedAt) {
    this.id = Objects.requireNonNull(id);
    this.userId = Objects.requireNonNull(userId);
    if (tokenHash == null || !tokenHash.matches("[0-9a-f]{64}"))
      throw new BusinessRuleException("Invalid password reset token hash");
    this.createdAt = Objects.requireNonNull(createdAt);
    this.expiresAt = Objects.requireNonNull(expiresAt);
    if (!expiresAt.isAfter(createdAt)) throw new BusinessRuleException("Invalid token lifetime");
    if (usedAt != null && revokedAt != null)
      throw new BusinessRuleException("A token cannot be used and revoked");
    this.tokenHash = tokenHash;
    this.usedAt = usedAt;
    this.revokedAt = revokedAt;
  }

  public static PasswordResetToken issue(
      PasswordResetTokenId id,
      UserId userId,
      String tokenHash,
      Instant createdAt,
      Instant expiresAt) {
    var result = new PasswordResetToken(id, userId, tokenHash, createdAt, expiresAt, null, null);
    result.recordEvent(new PasswordResetTokenChanged(id, createdAt));
    return result;
  }

  public static PasswordResetToken restore(
      PasswordResetTokenId id,
      UserId userId,
      String tokenHash,
      Instant createdAt,
      Instant expiresAt,
      Instant usedAt,
      Instant revokedAt) {
    return new PasswordResetToken(id, userId, tokenHash, createdAt, expiresAt, usedAt, revokedAt);
  }

  public PasswordResetToken consume(Instant now) {
    if (usedAt != null || revokedAt != null || !expiresAt.isAfter(now))
      throw new BusinessRuleException("Password reset token is invalid or expired");
    var result = new PasswordResetToken(id, userId, tokenHash, createdAt, expiresAt, now, null);
    result.recordEvent(new PasswordResetTokenChanged(id, now));
    return result;
  }

  public PasswordResetTokenId id() {
    return id;
  }

  public UserId userId() {
    return userId;
  }

  public String tokenHash() {
    return tokenHash;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant expiresAt() {
    return expiresAt;
  }

  public Instant usedAt() {
    return usedAt;
  }

  public Instant revokedAt() {
    return revokedAt;
  }
}
