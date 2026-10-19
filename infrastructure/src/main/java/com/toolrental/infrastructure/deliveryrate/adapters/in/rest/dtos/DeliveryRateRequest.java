package com.toolrental.infrastructure.deliveryrate.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record DeliveryRateRequest(@NotNull @PositiveOrZero BigDecimal price) {}
