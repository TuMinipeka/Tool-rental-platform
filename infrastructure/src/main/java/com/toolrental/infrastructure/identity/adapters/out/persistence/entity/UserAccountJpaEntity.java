package com.toolrental.infrastructure.identity.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserAccountJpaEntity {
  @Id
  @Column(name = "user_id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "account_status", nullable = false, length = 20)
  private String accountStatus;

  protected UserAccountJpaEntity() {}

  public UUID getId() {
    return id;
  }

  public String getAccountStatus() {
    return accountStatus;
  }
}
