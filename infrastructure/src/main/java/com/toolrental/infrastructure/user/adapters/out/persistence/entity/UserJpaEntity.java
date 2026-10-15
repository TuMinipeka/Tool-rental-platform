package com.toolrental.infrastructure.user.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserJpaEntity {
  @Id
  @Column(name = "user_id")
  private UUID id;

  @Column(name = "email", nullable = false, length = 320)
  private String email;

  @Column(name = "display_name", nullable = false, length = 200)
  private String displayName;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(name = "account_status", nullable = false, length = 20)
  private String accountStatus;

  @Column(name = "registered_at", nullable = false)
  private Instant registeredAt;

  protected UserJpaEntity() {}

  public UserJpaEntity(
      UUID id,
      String email,
      String displayName,
      String passwordHash,
      String accountStatus,
      Instant registeredAt) {
    this.id = id;
    this.email = email;
    this.displayName = displayName;
    this.passwordHash = passwordHash;
    this.accountStatus = accountStatus;
    this.registeredAt = registeredAt;
  }

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public String getAccountStatus() {
    return accountStatus;
  }

  public Instant getRegisteredAt() {
    return registeredAt;
  }
}
