package com.toolrental.infrastructure.supplier.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SupplierRequest(
    @NotNull UUID userId,
    @NotBlank String documentType,
    @NotBlank String documentNumber,
    @NotBlank String businessName) {}
