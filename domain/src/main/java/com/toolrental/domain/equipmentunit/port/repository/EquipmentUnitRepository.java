package com.toolrental.domain.equipmentunit.port.repository;

import com.toolrental.domain.equipmentunit.model.aggregate.EquipmentUnit;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import java.util.List;
import java.util.Optional;

public interface EquipmentUnitRepository {
  Optional<EquipmentUnit> findById(EquipmentUnitId id);

  List<EquipmentUnit> findAll();

  void save(EquipmentUnit aggregate);
}
