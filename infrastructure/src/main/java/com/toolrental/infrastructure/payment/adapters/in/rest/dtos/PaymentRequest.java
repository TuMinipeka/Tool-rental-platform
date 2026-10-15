package com.toolrental.infrastructure.payment.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
    @NotNull UUID reservationId,
    @NotBlank String processor,
    @NotBlank String externalReference,
    @NotNull BigDecimal amount) {}
