package com.toolrental.application.reservation.command;

import com.toolrental.domain.catalog.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.common.model.valueobject.CustomerId;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import java.util.List;
import java.util.Objects;

public record RequestReservationCommand(
    CustomerId customerId, ReservationPeriod period, List<EquipmentUnitId> equipmentUnits) {
  public RequestReservationCommand {
    Objects.requireNonNull(customerId);
    Objects.requireNonNull(period);
    equipmentUnits = List.copyOf(equipmentUnits);
  }
}
