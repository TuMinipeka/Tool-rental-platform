package com.toolrental.infrastructure.user.adapters.in.rest.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierAccountRegistrationRequest(
    @NotBlank @Email String email,
    @NotBlank String displayName,
    @NotBlank @Size(min = 12) String password,
    @NotBlank String documentType,
    @NotBlank String documentNumber,
    @NotBlank String businessName) {}
