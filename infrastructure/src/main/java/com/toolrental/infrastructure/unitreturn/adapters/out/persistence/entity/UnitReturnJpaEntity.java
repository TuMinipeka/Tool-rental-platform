package com.toolrental.infrastructure.unitreturn.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "unit_returns")
public class UnitReturnJpaEntity {
  @EmbeddedId private UnitReturnJpaId id;

  @Column(name = "received_at", nullable = false)
  private Instant receivedAt;

  @Column(name = "inbound_condition", nullable = false, length = 500)
  private String inboundCondition;

  @Column(name = "notes", nullable = true, length = 2000)
  private String notes;

  @Column(name = "late_days", nullable = false)
  private int lateDays;

  @Column(name = "late_fee", nullable = false, precision = 19, scale = 4)
  private BigDecimal lateFee;

  @Column(name = "late_fee_currency", nullable = false, length = 3)
  private String lateFeeCurrency;

  protected UnitReturnJpaEntity() {}

  public UnitReturnJpaEntity(
      UnitReturnJpaId id,
      Instant receivedAt,
      String inboundCondition,
      String notes,
      int lateDays,
      BigDecimal lateFee,
      String lateFeeCurrency) {
    this.id = id;
    this.receivedAt = receivedAt;
    this.inboundCondition = inboundCondition;
    this.notes = notes;
    this.lateDays = lateDays;
    this.lateFee = lateFee;
    this.lateFeeCurrency = lateFeeCurrency;
  }

  public UnitReturnJpaId getId() {
    return id;
  }

  public Instant getReceivedAt() {
    return receivedAt;
  }

  public String getInboundCondition() {
    return inboundCondition;
  }

  public String getNotes() {
    return notes;
  }

  public int getLateDays() {
    return lateDays;
  }

  public BigDecimal getLateFee() {
    return lateFee;
  }

  public String getLateFeeCurrency() {
    return lateFeeCurrency;
  }
}
