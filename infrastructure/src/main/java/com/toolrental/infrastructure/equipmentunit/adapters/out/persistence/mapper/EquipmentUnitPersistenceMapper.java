package com.toolrental.infrastructure.equipmentunit.adapters.out.persistence.mapper;

import com.toolrental.domain.equipmentunit.model.aggregate.EquipmentUnit;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.equipmentunit.model.valueobject.OperationalStatus;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import com.toolrental.infrastructure.equipmentunit.adapters.out.persistence.entity.EquipmentUnitJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class EquipmentUnitPersistenceMapper {
  public EquipmentUnit toDomain(EquipmentUnitJpaEntity entity) {
    return EquipmentUnit.restore(
        new EquipmentUnitId(entity.getId()),
        new ToolId(entity.getToolId()),
        entity.getInventoryCode(),
        OperationalStatus.valueOf(entity.getOperationalStatus()), entity.getSerialNumber(), entity.getCurrentCondition());
  }

  public EquipmentUnitJpaEntity toEntity(EquipmentUnit aggregate) {
    var entity = new EquipmentUnitJpaEntity(
        aggregate.id().value(),
        aggregate.toolId().value(),
        aggregate.inventoryCode(),
        aggregate.status().name()); entity.preserveSerialNumber(aggregate.serialNumber()); entity.preserveCurrentCondition(aggregate.currentCondition()); return entity;
  }
}
