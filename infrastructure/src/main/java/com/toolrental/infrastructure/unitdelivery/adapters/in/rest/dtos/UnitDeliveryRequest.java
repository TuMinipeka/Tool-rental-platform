package com.toolrental.infrastructure.unitdelivery.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record UnitDeliveryRequest(
    @NotNull UUID reservationId,
    @NotNull UUID equipmentUnitId,
    @NotNull Instant scheduledAt,
    String outboundCondition,
    @NotBlank String addressLine,
    @NotBlank String city,
    @NotBlank String region,
    @NotBlank String country) {}
