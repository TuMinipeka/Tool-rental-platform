package com.toolrental.application.role.dto;

import com.toolrental.domain.role.model.aggregate.Role;

public record RoleResponse(Short roleId, String code, String description) {
  public static RoleResponse from(Role value) {
    return new RoleResponse(value.id().value(), value.code(), value.description());
  }
}
