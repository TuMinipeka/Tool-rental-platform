package com.toolrental.domain.equipmentunit.model.aggregate;

import com.toolrental.domain.equipmentunit.event.EquipmentUnitRegistered;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.equipmentunit.model.valueobject.OperationalStatus;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.common.model.valueobject.Validation;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class EquipmentUnit extends AggregateRoot {
  private final EquipmentUnitId id;
  private final ToolId toolId;
  private final String inventoryCode;
  private OperationalStatus status;
 private String serialNumber;
 private String currentCondition = "GOOD";

  private EquipmentUnit(EquipmentUnitId id, ToolId tool, String code, OperationalStatus status) {
    this.id = Objects.requireNonNull(id);
    this.status = Objects.requireNonNull(status);
    toolId = Objects.requireNonNull(tool);
    inventoryCode = Validation.requiredText(code, "inventoryCode");
  }

  public static EquipmentUnit register(ToolId tool, String code, Clock clock) {
    var equipmentUnit =
        new EquipmentUnit(EquipmentUnitId.generate(), tool, code, OperationalStatus.OPERATIONAL);
    equipmentUnit.recordEvent(new EquipmentUnitRegistered(equipmentUnit.id, Instant.now(clock)));
    return equipmentUnit;
  }

  public static EquipmentUnit restore(
      EquipmentUnitId id, ToolId tool, String code, OperationalStatus status) {
    return new EquipmentUnit(id, tool, code, status);
  }

  public static EquipmentUnit restore(EquipmentUnitId id, ToolId tool, String code, OperationalStatus status, String serialNumber, String currentCondition) {
    var result = new EquipmentUnit(id, tool, code, status); result.serialNumber = serialNumber;
    result.currentCondition = Validation.requiredText(currentCondition, "currentCondition"); return result;
  }
  public String serialNumber() { return serialNumber; }
  public String currentCondition() { return currentCondition; }
  public void sendToMaintenance() {
    requireNotRetired();
    status = OperationalStatus.MAINTENANCE;
  }

  public void makeOperational() {
    requireNotRetired();
    status = OperationalStatus.OPERATIONAL;
  }

  public void retire() {
    requireNotRetired();
    status = OperationalStatus.RETIRED;
  }

  private void requireNotRetired() {
    if (status == OperationalStatus.RETIRED)
      throw new BusinessRuleException("A retired equipment unit cannot change status");
  }

  public EquipmentUnitId id() {
    return id;
  }

  public ToolId toolId() {
    return toolId;
  }

  public String inventoryCode() {
    return inventoryCode;
  }

  public OperationalStatus status() {
    return status;
  }

  public boolean isOperational() {
    return status == OperationalStatus.OPERATIONAL;
  }
}
