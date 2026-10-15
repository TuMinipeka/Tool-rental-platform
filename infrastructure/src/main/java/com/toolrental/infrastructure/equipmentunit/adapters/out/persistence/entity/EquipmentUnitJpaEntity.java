package com.toolrental.infrastructure.equipmentunit.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "equipment_units")
public class EquipmentUnitJpaEntity {
  @Id
  @Column(name = "equipment_unit_id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "tool_id", nullable = false, updatable = false)
  private UUID toolId;

  @Column(name = "inventory_code", nullable = false, length = 120)
  private String inventoryCode;

  @Column(name = "operational_status", nullable = false, length = 20)
  private String operationalStatus;

  @Column(name = "serial_number", length = 160)
  private String serialNumber;

  public String getSerialNumber() {
    return serialNumber;
  }

  public void preserveSerialNumber(String value) {
    serialNumber = value;
  }

  @Column(name = "current_condition", length = 40)
  private String currentCondition = "GOOD";

  public String getCurrentCondition() {
    return currentCondition;
  }

  public void preserveCurrentCondition(String value) {
    currentCondition = value;
  }

  protected EquipmentUnitJpaEntity() {}

  public EquipmentUnitJpaEntity(
      UUID id, UUID toolId, String inventoryCode, String operationalStatus) {
    this.id = id;
    this.toolId = toolId;
    this.inventoryCode = inventoryCode;
    this.operationalStatus = operationalStatus;
  }

  public UUID getId() {
    return id;
  }

  public UUID getToolId() {
    return toolId;
  }

  public String getInventoryCode() {
    return inventoryCode;
  }

  public String getOperationalStatus() {
    return operationalStatus;
  }
}
