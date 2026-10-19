package com.toolrental.application.tool.dto;

import com.toolrental.domain.equipmentunit.model.aggregate.EquipmentUnit;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import java.util.List;
import java.util.UUID;

public record ToolAvailabilityResponse(
    UUID toolId,
    ReservationPeriod period,
    int availableUnitCount,
    List<UUID> availableEquipmentUnitIds) {
  public ToolAvailabilityResponse {
    availableEquipmentUnitIds = List.copyOf(availableEquipmentUnitIds);
  }

  public static ToolAvailabilityResponse from(
      UUID toolId, ReservationPeriod period, List<EquipmentUnit> units) {
    var ids = units.stream().map(unit -> unit.id().value()).toList();
    return new ToolAvailabilityResponse(toolId, period, ids.size(), ids);
  }
}
