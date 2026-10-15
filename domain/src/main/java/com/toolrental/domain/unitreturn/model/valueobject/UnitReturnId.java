package com.toolrental.domain.unitreturn.model.valueobject;

import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import java.util.Objects;

public record UnitReturnId(ReservationId reservationId, EquipmentUnitId equipmentUnitId) {
  public UnitReturnId {
    Objects.requireNonNull(reservationId);
    Objects.requireNonNull(equipmentUnitId);
  }
}
