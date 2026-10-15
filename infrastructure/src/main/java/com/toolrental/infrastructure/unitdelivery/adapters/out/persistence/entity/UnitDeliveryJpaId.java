package com.toolrental.infrastructure.unitdelivery.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class UnitDeliveryJpaId implements Serializable {
  @Column(name = "reservation_id")
  private UUID reservationId;

  @Column(name = "equipment_unit_id")
  private UUID equipmentUnitId;

  protected UnitDeliveryJpaId() {}

  public UnitDeliveryJpaId(UUID reservationId, UUID equipmentUnitId) {
    this.reservationId = reservationId;
    this.equipmentUnitId = equipmentUnitId;
  }

  public UUID getReservationId() {
    return reservationId;
  }

  public UUID getEquipmentUnitId() {
    return equipmentUnitId;
  }

  public boolean equals(Object other) {
    return other instanceof UnitDeliveryJpaId key
        && Objects.equals(reservationId, key.reservationId)
        && Objects.equals(equipmentUnitId, key.equipmentUnitId);
  }

  public int hashCode() {
    return Objects.hash(reservationId, equipmentUnitId);
  }
}
