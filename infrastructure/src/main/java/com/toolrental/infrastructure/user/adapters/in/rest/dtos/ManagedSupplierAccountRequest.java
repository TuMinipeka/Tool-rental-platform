package com.toolrental.infrastructure.user.adapters.in.rest.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ManagedSupplierAccountRequest(
    @NotBlank @Email @Size(max = 320) String email,
    @NotBlank @Size(max = 200) String displayName,
    @NotBlank @Size(min = 12, max = 72) String password,
    @NotBlank @Size(max = 30) String documentType,
    @NotBlank @Size(max = 40) String documentNumber,
    @NotBlank @Size(max = 200) String businessName) {}
