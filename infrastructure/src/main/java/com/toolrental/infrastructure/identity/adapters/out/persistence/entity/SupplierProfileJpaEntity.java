package com.toolrental.infrastructure.identity.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "suppliers")
public class SupplierProfileJpaEntity {
  @Id
  @Column(name = "user_id", nullable = false, updatable = false)
  private UUID id;

  protected SupplierProfileJpaEntity() {}

  public UUID getId() {
    return id;
  }
}
