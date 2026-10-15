package com.toolrental.domain.reservationitem.model.entity;

import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.common.model.valueobject.SupplierId;
import java.util.Objects;

/** Local identity: equipmentUnitId within a Reservation. The rate is a snapshot. */
public record ReservationItem(
    EquipmentUnitId equipmentUnitId, SupplierId supplierId, Money dailyRate) {
  public ReservationItem {
    Objects.requireNonNull(equipmentUnitId);
    Objects.requireNonNull(supplierId);
    Objects.requireNonNull(dailyRate);
  }
}
