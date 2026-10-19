package com.toolrental.infrastructure.cancellationpolicy.adapters.in.rest.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CancellationPolicyRequest(
    @Min(0) int cutoffHours,
    @NotNull @DecimalMin("0") @DecimalMax("1") BigDecimal lateCancellationRate,
    @NotNull @DecimalMin("0") @DecimalMax("1") BigDecimal noShowRate,
    @Min(0) int noShowGraceMinutes) {}
