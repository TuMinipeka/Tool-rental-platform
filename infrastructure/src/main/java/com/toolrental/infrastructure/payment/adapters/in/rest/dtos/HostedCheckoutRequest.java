package com.toolrental.infrastructure.payment.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record HostedCheckoutRequest(
    @NotNull UUID reservationId,
    @NotBlank String paymentMethod,
    String cardNetwork,
    @Pattern(regexp = "[0-9]{4}") String cardLastFour,
    Short cardExpiryMonth,
    Short cardExpiryYear) {}
