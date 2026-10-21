package com.toolrental.infrastructure.tool.adapters.in.rest.dtos;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record ToolRequest(
    @NotNull UUID categoryId,
    @NotBlank @Size(max = 160) String name,
    @NotBlank @Size(max = 2000) String description,
    @NotBlank @Size(max = 120) String serviceCity,
    @NotNull @PositiveOrZero @Digits(integer = 15, fraction = 4) BigDecimal dailyRate,
    @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency,
    @NotNull Set<String> imageUrls,
    boolean published) {}
