package com.toolrental.infrastructure.role.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotBlank;

public record RoleRequest(@NotBlank String code, @NotBlank String description) {}
