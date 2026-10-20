package com.toolrental.infrastructure.payment.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record HostedCheckoutRequest(@NotNull UUID reservationId) {}
