package com.toolrental.domain.userrole.model.valueobject;

import com.toolrental.domain.role.model.valueobject.RoleId;
import com.toolrental.domain.user.model.valueobject.UserId;
import java.util.Objects;

public record UserRoleId(UserId userId, RoleId roleId) {
  public UserRoleId {
    Objects.requireNonNull(userId);
    Objects.requireNonNull(roleId);
  }
}
