package com.toolrental.application.catalog.dto;

import com.toolrental.domain.catalog.model.aggregate.EquipmentUnit;
import com.toolrental.domain.catalog.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.catalog.model.valueobject.OperationalStatus;
import com.toolrental.domain.catalog.model.valueobject.ToolId;

public record EquipmentUnitResponse(
    EquipmentUnitId id, ToolId toolId, String inventoryCode, OperationalStatus status) {
  public static EquipmentUnitResponse from(EquipmentUnit c) {
    return new EquipmentUnitResponse(c.id(), c.toolId(), c.inventoryCode(), c.status());
  }
}
