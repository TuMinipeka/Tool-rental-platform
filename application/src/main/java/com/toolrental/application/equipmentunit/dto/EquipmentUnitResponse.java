package com.toolrental.application.equipmentunit.dto;

import com.toolrental.domain.equipmentunit.model.aggregate.EquipmentUnit;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.equipmentunit.model.valueobject.OperationalStatus;
import com.toolrental.domain.tool.model.valueobject.ToolId;

public record EquipmentUnitResponse(
    EquipmentUnitId id, ToolId toolId, String inventoryCode, OperationalStatus status) {
  public static EquipmentUnitResponse from(EquipmentUnit c) {
    return new EquipmentUnitResponse(c.id(), c.toolId(), c.inventoryCode(), c.status());
  }
}
