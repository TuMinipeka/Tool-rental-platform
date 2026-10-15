package com.toolrental.infrastructure.identity.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class CustomerProfileJpaEntity {
  @Id
  @Column(name = "user_id", nullable = false, updatable = false)
  private UUID id;

  protected CustomerProfileJpaEntity() {}

  public UUID getId() {
    return id;
  }
}
