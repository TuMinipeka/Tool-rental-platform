package com.toolrental.infrastructure.payment.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SandboxCheckoutRequest(
    @NotNull UUID reservationId,
    @NotBlank String paymentMethod,
    String cardNetwork,
    String cardLastFour,
    Short cardExpiryMonth,
    Short cardExpiryYear) {}
