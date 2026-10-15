package com.toolrental.domain.equipmentunit.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record EquipmentUnitId(UUID value) {
  public EquipmentUnitId {
    Objects.requireNonNull(value, "EquipmentUnitId");
  }

  public static EquipmentUnitId generate() {
    return new EquipmentUnitId(UUID.randomUUID());
  }
}
