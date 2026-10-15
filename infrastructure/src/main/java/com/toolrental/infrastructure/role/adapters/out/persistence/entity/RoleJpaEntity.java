package com.toolrental.infrastructure.role.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public class RoleJpaEntity {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "role_id")
  private Short id;

  @Column(name = "code", nullable = false, length = 40)
  private String code;

  @Column(name = "description", nullable = false, length = 200)
  private String description;

  protected RoleJpaEntity() {}

  public RoleJpaEntity(Short id, String code, String description) {
    this.id = id;
    this.code = code;
    this.description = description;
  }

  public Short getId() {
    return id;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }
}
