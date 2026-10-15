package com.toolrental.application.equipmentunit.command;

import com.toolrental.domain.tool.model.valueobject.ToolId;

public record RegisterEquipmentUnitCommand(ToolId toolId, String inventoryCode) {}
