package com.toolrental.application.userrole.dto;

import com.toolrental.domain.userrole.model.aggregate.UserRole;
import java.util.UUID;

public record UserRoleResponse(UUID userId, Short roleId) {
  public static UserRoleResponse from(UserRole value) {
    return new UserRoleResponse(value.id().userId().value(), value.id().roleId().value());
  }
}
