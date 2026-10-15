package com.toolrental.infrastructure.supplier.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "suppliers")
public class SupplierJpaEntity {
  @Id
  @Column(name = "user_id", nullable = false, updatable = false)
  private UUID id;

  protected SupplierJpaEntity() {}

  public UUID getId() {
    return id;
  }
}
