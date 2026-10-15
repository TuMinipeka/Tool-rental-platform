package com.toolrental.infrastructure.equipmentunit.adapters.out.persistence;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.equipmentunit.model.aggregate.EquipmentUnit;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.equipmentunit.port.repository.AtomicEquipmentUnitRegistration;
import com.toolrental.domain.equipmentunit.port.repository.EquipmentUnitRepository;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import com.toolrental.infrastructure.equipmentunit.adapters.out.persistence.mapper.EquipmentUnitPersistenceMapper;
import com.toolrental.infrastructure.equipmentunit.adapters.out.persistence.repository.EquipmentUnitJpaRepository;
import com.toolrental.infrastructure.supplier.adapters.out.persistence.repository.SupplierJpaRepository;
import com.toolrental.infrastructure.tool.adapters.out.persistence.repository.ToolJpaRepository;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class EquipmentUnitPersistenceAdapter
    implements EquipmentUnitRepository, AtomicEquipmentUnitRegistration {
  private final EquipmentUnitJpaRepository equipmentUnits;
  private final ToolJpaRepository tools;
  private final SupplierJpaRepository suppliers;
  private final EquipmentUnitPersistenceMapper mapper;

  public EquipmentUnitPersistenceAdapter(
      EquipmentUnitJpaRepository equipmentUnits,
      ToolJpaRepository tools,
      SupplierJpaRepository suppliers,
      EquipmentUnitPersistenceMapper mapper) {
    this.equipmentUnits = equipmentUnits;
    this.tools = tools;
    this.suppliers = suppliers;
    this.mapper = mapper;
  }

  @Override
  public Optional<EquipmentUnit> findById(EquipmentUnitId id) {
    return equipmentUnits.findByIdForUpdate(id.value()).map(mapper::toDomain);
  }

  @Override
  public List<EquipmentUnit> findAll() {
    return equipmentUnits.findAll().stream().map(mapper::toDomain).toList();
  }

  @Override
  @Transactional
  public void save(EquipmentUnit aggregate) {
    equipmentUnits.save(mapper.toEntity(aggregate));
  }

  @Override
  @Transactional
  public EquipmentUnit register(ToolId toolId, String inventoryCode, Clock clock) {
    var tool =
        tools
            .findByIdForUpdate(toolId.value())
            .orElseThrow(() -> new BusinessRuleException("Tool does not exist"));
    suppliers
        .findByIdForUpdate(tool.getSupplierId())
        .orElseThrow(() -> new BusinessRuleException("Supplier does not exist"));
    var equipmentUnit = EquipmentUnit.register(toolId, inventoryCode, clock);
    if (equipmentUnits.countBySupplierAndInventoryCode(
            tool.getSupplierId(), equipmentUnit.inventoryCode())
        > 0) {
      throw new BusinessRuleException("Inventory code already exists for this supplier");
    }
    equipmentUnits.saveAndFlush(mapper.toEntity(equipmentUnit));
    return equipmentUnit;
  }
}
