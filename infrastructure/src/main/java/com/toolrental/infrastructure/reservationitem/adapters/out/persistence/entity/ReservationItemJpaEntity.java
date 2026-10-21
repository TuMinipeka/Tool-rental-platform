package com.toolrental.infrastructure.reservationitem.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "reservation_items")
public class ReservationItemJpaEntity {
  @EmbeddedId private ReservationItemJpaId id;

  @Column(name = "agreed_daily_rate", nullable = false, precision = 19, scale = 4)
  private BigDecimal agreedDailyRate;

  public UUID getEquipmentUnitId() {
    return id.getEquipmentUnitId();
  }

  protected ReservationItemJpaEntity() {}

  public ReservationItemJpaEntity(ReservationItemJpaId id, BigDecimal agreedDailyRate) {
    this.id = id;
    this.agreedDailyRate = agreedDailyRate;
  }

  public ReservationItemJpaId getId() {
    return id;
  }

  public BigDecimal getAgreedDailyRate() {
    return agreedDailyRate;
  }
}
