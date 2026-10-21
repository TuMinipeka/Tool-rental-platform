package com.toolrental.infrastructure.user.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
    @NotBlank String currentPassword, @NotBlank String newPassword) {}
