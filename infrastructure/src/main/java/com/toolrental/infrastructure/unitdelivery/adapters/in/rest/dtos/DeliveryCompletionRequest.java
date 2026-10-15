package com.toolrental.infrastructure.unitdelivery.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotBlank;

public record DeliveryCompletionRequest(@NotBlank String condition) {}
