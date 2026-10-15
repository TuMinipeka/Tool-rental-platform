package com.toolrental.infrastructure.toolimage.adapters.out.persistence.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "tool_images")
public class ToolImageJpaEntity {
  @EmbeddedId private ToolImageJpaId id;

  protected ToolImageJpaEntity() {}

  public ToolImageJpaEntity(ToolImageJpaId id) {
    this.id = id;
  }

  public ToolImageJpaId getId() {
    return id;
  }
}
