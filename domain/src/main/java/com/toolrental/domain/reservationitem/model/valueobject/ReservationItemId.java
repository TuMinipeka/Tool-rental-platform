package com.toolrental.domain.reservationitem.model.valueobject;

import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import java.util.Objects;

public record ReservationItemId(ReservationId reservationId, EquipmentUnitId equipmentUnitId) {
  public ReservationItemId {
    Objects.requireNonNull(reservationId);
    Objects.requireNonNull(equipmentUnitId);
  }
}
