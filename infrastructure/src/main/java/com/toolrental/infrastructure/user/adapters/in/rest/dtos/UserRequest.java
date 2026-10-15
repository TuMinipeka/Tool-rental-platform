package com.toolrental.infrastructure.user.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotBlank;

public record UserRequest(
    @NotBlank String email, @NotBlank String displayName, @NotBlank String password) {}
