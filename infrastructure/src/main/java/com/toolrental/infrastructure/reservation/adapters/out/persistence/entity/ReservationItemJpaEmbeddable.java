package com.toolrental.infrastructure.reservation.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ReservationItemJpaEmbeddable {
  @Column(name = "equipment_unit_id", nullable = false)
  private UUID equipmentUnitId;

  @Column(name = "agreed_daily_rate", nullable = false, precision = 19, scale = 4)
  private BigDecimal agreedDailyRate;

  protected ReservationItemJpaEmbeddable() {}

  public ReservationItemJpaEmbeddable(UUID equipmentUnitId, BigDecimal agreedDailyRate) {
    this.equipmentUnitId = equipmentUnitId;
    this.agreedDailyRate = agreedDailyRate;
  }

  public UUID getEquipmentUnitId() {
    return equipmentUnitId;
  }

  public BigDecimal getAgreedDailyRate() {
    return agreedDailyRate;
  }

  @Override
  public boolean equals(Object other) {
    return this == other
        || other instanceof ReservationItemJpaEmbeddable item
            && Objects.equals(equipmentUnitId, item.equipmentUnitId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(equipmentUnitId);
  }
}
