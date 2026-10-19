package com.toolrental.domain.reservation.port.repository;

import com.toolrental.domain.equipmentunit.model.aggregate.EquipmentUnit;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import java.util.List;

public interface AvailabilityRepository {
  List<EquipmentUnit> findAvailableUnits(ToolId toolId, ReservationPeriod period);
}
