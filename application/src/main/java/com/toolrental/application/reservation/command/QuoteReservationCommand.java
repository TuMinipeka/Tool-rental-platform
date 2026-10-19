package com.toolrental.application.reservation.command;

import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import java.util.List;

public record QuoteReservationCommand(
    ReservationPeriod period, List<EquipmentUnitId> equipmentUnitIds) {
  public QuoteReservationCommand {
    equipmentUnitIds = List.copyOf(equipmentUnitIds);
  }
}
