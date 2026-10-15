package com.toolrental.infrastructure.damagereport.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record DamageReportRequest(
    @NotNull UUID reservationId,
    @NotNull UUID equipmentUnitId,
    @NotBlank String description,
    BigDecimal assessedCost) {}
