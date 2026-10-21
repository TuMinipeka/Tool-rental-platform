package com.toolrental.infrastructure.unitreturn.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UnitReturnRequest(
    @NotNull UUID reservationId,
    @NotNull UUID equipmentUnitId,
    @NotBlank String inboundCondition,
    String notes) {}
