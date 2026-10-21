package com.toolrental.infrastructure.usersession.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_sessions")
public class UserSessionJpaEntity {
  @Id
  @Column(name = "session_id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "family_id", nullable = false, updatable = false)
  private UUID familyId;

  @Column(name = "user_id", nullable = false, updatable = false)
  private UUID userId;

  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.CHAR)
  @Column(name = "refresh_token_hash", nullable = false, length = 64, updatable = false)
  private String refreshTokenHash;

  @Column(name = "session_status", nullable = false, length = 20)
  private String status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "rotated_at")
  private Instant rotatedAt;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  @Column(name = "replaced_by_session_id")
  private UUID replacedBySessionId;

  @Column(name = "client_label", length = 300)
  private String clientLabel;

  protected UserSessionJpaEntity() {}

  public UserSessionJpaEntity(
      UUID id,
      UUID familyId,
      UUID userId,
      String refreshTokenHash,
      String status,
      Instant createdAt,
      Instant expiresAt,
      Instant rotatedAt,
      Instant revokedAt,
      UUID replacedBySessionId,
      String clientLabel) {
    this.id = id;
    this.familyId = familyId;
    this.userId = userId;
    this.refreshTokenHash = refreshTokenHash;
    this.status = status;
    this.createdAt = createdAt;
    this.expiresAt = expiresAt;
    this.rotatedAt = rotatedAt;
    this.revokedAt = revokedAt;
    this.replacedBySessionId = replacedBySessionId;
    this.clientLabel = clientLabel;
  }

  public UUID getId() {
    return id;
  }

  public UUID getFamilyId() {
    return familyId;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getRefreshTokenHash() {
    return refreshTokenHash;
  }

  public String getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public Instant getRotatedAt() {
    return rotatedAt;
  }

  public Instant getRevokedAt() {
    return revokedAt;
  }

  public UUID getReplacedBySessionId() {
    return replacedBySessionId;
  }

  public String getClientLabel() {
    return clientLabel;
  }
}
