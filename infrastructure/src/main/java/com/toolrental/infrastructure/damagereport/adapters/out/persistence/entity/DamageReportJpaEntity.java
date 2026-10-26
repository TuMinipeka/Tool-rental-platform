package com.toolrental.infrastructure.damagereport.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.security.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "damage_reports")
public class DamageReportJpaEntity {
  @Id
  @Column(name = "damage_report_id")
  private UUID id;

  @Column(name = "reservation_id", nullable = false)
  private UUID reservationId;

  @Column(name = "equipment_unit_id", nullable = false)
  private UUID equipmentUnitId;

  @Column(name = "reported_at", nullable = false)
  private Instant reportedAt;

  @Column (name= "date_report", nullable = false)
  private Timestamp date_report;

  @Column(name = "description", nullable = false, length = 2000)
  private String description;

  @Column(name = "report_status", nullable = false, length = 20)
  private String reportStatus;

  @Column(name = "assessed_cost", nullable = true, precision = 19, scale = 4)
  private BigDecimal assessedCost;

  protected DamageReportJpaEntity() {}

  public DamageReportJpaEntity(
      UUID id,
      UUID reservationId,
      UUID equipmentUnitId,
      Instant reportedAt,
      String description,
      String reportStatus,
      BigDecimal assessedCost) {
    this.id = id;
    this.reservationId = reservationId;
    this.equipmentUnitId = equipmentUnitId;
    this.reportedAt = reportedAt;
    this.description = description;
    this.reportStatus = reportStatus;
    this.assessedCost = assessedCost;
  }

  public UUID getId() {
    return id;
  }

  public UUID getReservationId() {
    return reservationId;
  }

  public UUID getEquipmentUnitId() {
    return equipmentUnitId;
  }

  public Instant getReportedAt() {
    return reportedAt;
  }

  public String getDescription() {
    return description;
  }

  public String getReportStatus() {
    return reportStatus;
  }

  public BigDecimal getAssessedCost() {
    return assessedCost;
  }

  public Timestamp getDate_report() {
    return date_report;
  }
}
