package com.toolrental.domain.usersession.model.aggregate;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.domain.usersession.event.UserSessionChanged;
import com.toolrental.domain.usersession.model.valueobject.UserSessionId;
import com.toolrental.domain.usersession.model.valueobject.UserSessionRotation;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class UserSession extends AggregateRoot {
  private static final Set<String> STATUSES =
      Set.of("ACTIVE", "ROTATED", "REVOKED", "COMPROMISED", "EXPIRED");

  private final UserSessionId id;
  private final UUID familyId;
  private final UserId userId;
  private final String refreshTokenHash;
  private final String status;
  private final Instant createdAt;
  private final Instant expiresAt;
  private final Instant rotatedAt;
  private final Instant revokedAt;
  private final UserSessionId replacedBy;
  private final String clientLabel;

  private UserSession(
      UserSessionId id,
      UUID familyId,
      UserId userId,
      String refreshTokenHash,
      String status,
      Instant createdAt,
      Instant expiresAt,
      Instant rotatedAt,
      Instant revokedAt,
      UserSessionId replacedBy,
      String clientLabel) {
    this.id = Objects.requireNonNull(id);
    this.familyId = Objects.requireNonNull(familyId);
    this.userId = Objects.requireNonNull(userId);
    if (refreshTokenHash == null || !refreshTokenHash.matches("[0-9a-f]{64}"))
      throw new BusinessRuleException("Invalid refresh token hash");
    if (!STATUSES.contains(status)) throw new BusinessRuleException("Invalid session status");
    this.createdAt = Objects.requireNonNull(createdAt);
    this.expiresAt = Objects.requireNonNull(expiresAt);
    if (!expiresAt.isAfter(createdAt)) throw new BusinessRuleException("Invalid session lifetime");
    if (clientLabel != null && (clientLabel.isBlank() || clientLabel.length() > 300))
      throw new BusinessRuleException("Invalid client label");
    this.refreshTokenHash = refreshTokenHash;
    this.status = status;
    this.rotatedAt = rotatedAt;
    this.revokedAt = revokedAt;
    this.replacedBy = replacedBy;
    this.clientLabel = clientLabel;
  }

  public static UserSession start(
      UserSessionId id,
      UUID familyId,
      UserId userId,
      String refreshTokenHash,
      Instant createdAt,
      Instant expiresAt,
      String clientLabel) {
    var session =
        new UserSession(
            id,
            familyId,
            userId,
            refreshTokenHash,
            "ACTIVE",
            createdAt,
            expiresAt,
            null,
            null,
            null,
            clientLabel);
    session.recordEvent(new UserSessionChanged(id, createdAt));
    return session;
  }

  public static UserSession restore(
      UserSessionId id,
      UUID familyId,
      UserId userId,
      String refreshTokenHash,
      String status,
      Instant createdAt,
      Instant expiresAt,
      Instant rotatedAt,
      Instant revokedAt,
      UserSessionId replacedBy,
      String clientLabel) {
    return new UserSession(
        id,
        familyId,
        userId,
        refreshTokenHash,
        status,
        createdAt,
        expiresAt,
        rotatedAt,
        revokedAt,
        replacedBy,
        clientLabel);
  }

  public UserSessionRotation rotate(
      UserSessionId successorId, String successorHash, Instant now, Instant successorExpiresAt) {
    requireActive(now);
    var rotated =
        new UserSession(
            id,
            familyId,
            userId,
            refreshTokenHash,
            "ROTATED",
            createdAt,
            expiresAt,
            now,
            null,
            successorId,
            clientLabel);
    var successor =
        start(successorId, familyId, userId, successorHash, now, successorExpiresAt, clientLabel);
    rotated.recordEvent(new UserSessionChanged(id, now));
    return new UserSessionRotation(rotated, successor);
  }

  public UserSession revoke(String nextStatus, Instant now) {
    if (!Set.of("REVOKED", "COMPROMISED").contains(nextStatus))
      throw new BusinessRuleException("Invalid revocation status");
    if (Set.of("REVOKED", "COMPROMISED", "EXPIRED").contains(status)) return this;
    var result =
        new UserSession(
            id,
            familyId,
            userId,
            refreshTokenHash,
            nextStatus,
            createdAt,
            expiresAt,
            rotatedAt,
            now,
            replacedBy,
            clientLabel);
    result.recordEvent(new UserSessionChanged(id, now));
    return result;
  }

  public void requireActive(Instant now) {
    if (!"ACTIVE".equals(status) || !expiresAt.isAfter(now))
      throw new BusinessRuleException("Session is not active");
  }

  public boolean isRotatedBeyondGrace(Instant now, Duration grace) {
    return "ROTATED".equals(status) && rotatedAt != null && !now.isBefore(rotatedAt.plus(grace));
  }

  public UserSessionId id() {
    return id;
  }

  public UUID familyId() {
    return familyId;
  }

  public UserId userId() {
    return userId;
  }

  public String refreshTokenHash() {
    return refreshTokenHash;
  }

  public String status() {
    return status;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant expiresAt() {
    return expiresAt;
  }

  public Instant rotatedAt() {
    return rotatedAt;
  }

  public Instant revokedAt() {
    return revokedAt;
  }

  public UserSessionId replacedBy() {
    return replacedBy;
  }

  public String clientLabel() {
    return clientLabel;
  }
}
