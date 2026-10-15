package com.toolrental.infrastructure.userrole.adapters.out.persistence.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_roles")
public class UserRoleJpaEntity {
  @EmbeddedId private UserRoleJpaId id;

  protected UserRoleJpaEntity() {}

  public UserRoleJpaEntity(UserRoleJpaId id) {
    this.id = id;
  }

  public UserRoleJpaId getId() {
    return id;
  }
}
