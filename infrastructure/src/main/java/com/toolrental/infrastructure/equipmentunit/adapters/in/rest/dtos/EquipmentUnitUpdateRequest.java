package com.toolrental.infrastructure.equipmentunit.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotBlank;

public record EquipmentUnitUpdateRequest(
    String serialNumber, @NotBlank String currentCondition, @NotBlank String status) {}
