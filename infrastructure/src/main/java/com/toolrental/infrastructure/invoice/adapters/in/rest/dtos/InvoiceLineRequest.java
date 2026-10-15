package com.toolrental.infrastructure.invoice.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record InvoiceLineRequest(
    @NotBlank String description,
    @NotNull @Positive BigDecimal quantity,
    @NotNull @PositiveOrZero BigDecimal unitPrice) {}
