package com.toolrental.infrastructure.userrole.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UserRoleRequest(@NotNull UUID userId, @NotNull Short roleId) {}
