package com.toolrental.infrastructure.damagereport.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record AssessmentRequest(@NotNull @PositiveOrZero BigDecimal cost) {}
