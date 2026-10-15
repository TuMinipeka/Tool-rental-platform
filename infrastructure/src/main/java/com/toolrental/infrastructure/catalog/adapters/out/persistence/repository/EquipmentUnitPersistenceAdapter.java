package com.toolrental.infrastructure.catalog.adapters.out.persistence.repository;

import com.toolrental.domain.catalog.model.aggregate.EquipmentUnit;
import com.toolrental.domain.catalog.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.catalog.model.valueobject.ToolId;
import com.toolrental.domain.catalog.port.repository.AtomicEquipmentUnitRegistration;
import com.toolrental.domain.catalog.port.repository.EquipmentUnitRepository;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.infrastructure.catalog.adapters.out.persistence.mapper.EquipmentUnitPersistenceMapper;
import com.toolrental.infrastructure.identity.adapters.out.persistence.repository.SupplierProfileJpaRepository;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class EquipmentUnitPersistenceAdapter
    implements EquipmentUnitRepository, AtomicEquipmentUnitRegistration {
  private final EquipmentUnitJpaRepository equipmentUnits;
  private final ToolJpaRepository tools;
  private final SupplierProfileJpaRepository suppliers;
  private final EquipmentUnitPersistenceMapper mapper;

  public EquipmentUnitPersistenceAdapter(
      EquipmentUnitJpaRepository equipmentUnits,
      ToolJpaRepository tools,
      SupplierProfileJpaRepository suppliers,
      EquipmentUnitPersistenceMapper mapper) {
    this.equipmentUnits = equipmentUnits;
    this.tools = tools;
    this.suppliers = suppliers;
    this.mapper = mapper;
  }

  @Override
  public Optional<EquipmentUnit> findById(EquipmentUnitId id) {
    return equipmentUnits.findById(id.value()).map(mapper::toDomain);
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
