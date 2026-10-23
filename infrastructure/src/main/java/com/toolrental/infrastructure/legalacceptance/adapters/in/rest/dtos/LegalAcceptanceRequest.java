package com.toolrental.infrastructure.legalacceptance.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LegalAcceptanceRequest(
    @NotBlank @Size(max = 50) String documentType,
    @NotBlank @Size(max = 40) String documentVersion,
    @NotBlank @Size(max = 80) @Pattern(regexp = "[A-Za-z0-9_]+") String purpose) {}
