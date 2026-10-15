package com.toolrental.infrastructure.equipmentunit.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record EquipmentUnitRequest(
    @NotNull UUID toolId,
    @NotBlank @Size(max = 120) String inventoryCode,
    @Size(max = 160) String serialNumber,
    @NotBlank @Size(max = 40) String currentCondition,
    @NotBlank String status) {}
