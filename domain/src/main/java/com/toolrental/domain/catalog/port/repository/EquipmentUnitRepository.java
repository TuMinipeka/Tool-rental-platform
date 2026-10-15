package com.toolrental.domain.catalog.port.repository;

import com.toolrental.domain.catalog.model.aggregate.EquipmentUnit;
import com.toolrental.domain.catalog.model.valueobject.EquipmentUnitId;
import java.util.List;
import java.util.Optional;

public interface EquipmentUnitRepository {
  Optional<EquipmentUnit> findById(EquipmentUnitId id);

  List<EquipmentUnit> findAll();

  void save(EquipmentUnit aggregate);
}
