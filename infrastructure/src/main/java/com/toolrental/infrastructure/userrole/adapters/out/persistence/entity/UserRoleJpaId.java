package com.toolrental.infrastructure.userrole.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class UserRoleJpaId implements Serializable {
  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "role_id")
  private Short roleId;

  protected UserRoleJpaId() {}

  public UserRoleJpaId(UUID userId, Short roleId) {
    this.userId = userId;
    this.roleId = roleId;
  }

  public UUID getUserId() {
    return userId;
  }

  public Short getRoleId() {
    return roleId;
  }

  public boolean equals(Object other) {
    return other instanceof UserRoleJpaId key
        && Objects.equals(userId, key.userId)
        && Objects.equals(roleId, key.roleId);
  }

  public int hashCode() {
    return Objects.hash(userId, roleId);
  }
}
