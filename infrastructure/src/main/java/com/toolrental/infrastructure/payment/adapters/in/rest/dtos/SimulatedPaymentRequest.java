package com.toolrental.infrastructure.payment.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record SimulatedPaymentRequest(
    @NotNull UUID reservationId,
    @NotBlank String paymentStage,
    @NotBlank String paymentMethod,
    @NotBlank String idempotencyKey,
    String cardNetwork,
    @Pattern(regexp = "[0-9]{4}") String cardLastFour,
    Short cardExpiryMonth,
    Short cardExpiryYear,
    @NotBlank String simulationOutcome) {}
