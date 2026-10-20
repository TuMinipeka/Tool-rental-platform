package com.toolrental.infrastructure.tool.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ToolUpdateRequest(
    @NotBlank String name,
    @NotBlank String description,
    @NotBlank @Size(max = 120) String serviceCity,
    @NotNull @PositiveOrZero BigDecimal dailyRate,
    @NotBlank String currency,
    boolean published) {}
