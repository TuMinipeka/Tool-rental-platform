package com.toolrental.infrastructure.customer.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CustomerRequest(
    @NotNull UUID userId,
    @NotBlank String personType,
    @NotBlank String documentType,
    @NotBlank String documentNumber,
    @NotBlank String billingName) {}
