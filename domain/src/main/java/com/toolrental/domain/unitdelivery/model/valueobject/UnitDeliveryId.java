package com.toolrental.domain.unitdelivery.model.valueobject;

import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import java.util.Objects;

public record UnitDeliveryId(ReservationId reservationId, EquipmentUnitId equipmentUnitId) {
  public UnitDeliveryId {
    Objects.requireNonNull(reservationId);
    Objects.requireNonNull(equipmentUnitId);
  }
}
