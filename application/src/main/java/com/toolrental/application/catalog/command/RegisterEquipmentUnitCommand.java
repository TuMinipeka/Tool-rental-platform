package com.toolrental.application.catalog.command;

import com.toolrental.domain.catalog.model.valueobject.ToolId;

public record RegisterEquipmentUnitCommand(ToolId toolId, String inventoryCode) {}
