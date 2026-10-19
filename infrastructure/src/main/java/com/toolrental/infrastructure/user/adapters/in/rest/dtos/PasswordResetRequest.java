package com.toolrental.infrastructure.user.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequest(
    @NotBlank String token, @NotBlank @Size(min = 12) String newPassword) {}
