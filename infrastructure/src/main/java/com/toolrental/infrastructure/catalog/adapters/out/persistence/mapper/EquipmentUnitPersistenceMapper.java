package com.toolrental.infrastructure.catalog.adapters.out.persistence.mapper;

import com.toolrental.domain.catalog.model.aggregate.EquipmentUnit;
import com.toolrental.domain.catalog.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.catalog.model.valueobject.OperationalStatus;
import com.toolrental.domain.catalog.model.valueobject.ToolId;
import com.toolrental.infrastructure.catalog.adapters.out.persistence.entity.EquipmentUnitJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class EquipmentUnitPersistenceMapper {
  public EquipmentUnit toDomain(EquipmentUnitJpaEntity entity) {
    return EquipmentUnit.restore(
        new EquipmentUnitId(entity.getId()),
        new ToolId(entity.getToolId()),
        entity.getInventoryCode(),
        OperationalStatus.valueOf(entity.getOperationalStatus()));
  }

  public EquipmentUnitJpaEntity toEntity(EquipmentUnit aggregate) {
    return new EquipmentUnitJpaEntity(
        aggregate.id().value(),
        aggregate.toolId().value(),
        aggregate.inventoryCode(),
        aggregate.status().name());
  }
}
