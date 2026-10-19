package com.toolrental.infrastructure.user.adapters.in.rest.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordRecoveryRequest(@NotBlank @Email String email) {}
