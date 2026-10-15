package com.toolrental.infrastructure.category.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "categories")
public class CategoryJpaEntity {
  @Id
  @Column(name = "category_id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "name", nullable = false, length = 120)
  private String name;

  @Column(name = "description", length = 500)
  private String description;

  public String getDescription() {
    return description;
  }

  public void preserveDescription(String value) {
    description = value;
  }

  protected CategoryJpaEntity() {}

  public CategoryJpaEntity(UUID id, String name) {
    this.id = id;
    this.name = name;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
