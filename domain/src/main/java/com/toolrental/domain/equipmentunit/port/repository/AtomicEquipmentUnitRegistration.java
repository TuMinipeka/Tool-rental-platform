package com.toolrental.domain.equipmentunit.port.repository;

import com.toolrental.domain.equipmentunit.model.aggregate.EquipmentUnit;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import java.time.Clock;

/** Validates the tool and supplier-scoped inventory code, then saves atomically. */
public interface AtomicEquipmentUnitRegistration {
  EquipmentUnit register(ToolId toolId, String inventoryCode, Clock clock);
}
