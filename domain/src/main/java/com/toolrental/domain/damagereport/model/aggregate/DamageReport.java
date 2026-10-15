package com.toolrental.domain.damagereport.model.aggregate;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.damagereport.event.DamageReportChanged;
import com.toolrental.domain.damagereport.model.valueobject.DamageReportId;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public final class DamageReport extends AggregateRoot {
  private final DamageReportId id;
  private final ReservationId reservationId;
  private final EquipmentUnitId equipmentUnitId;
  private final Instant reportedAt;
  private final String description;
  private final String reportStatus;
  private final BigDecimal assessedCost;

  private DamageReport(
      DamageReportId id,
      ReservationId reservationId,
      EquipmentUnitId equipmentUnitId,
      Instant reportedAt,
      String description,
      String reportStatus,
      BigDecimal assessedCost) {
    Objects.requireNonNull(id);
    Objects.requireNonNull(reservationId, "reservationId is required");
    Objects.requireNonNull(equipmentUnitId, "equipmentUnitId is required");
    Objects.requireNonNull(reportedAt, "reportedAt is required");
    Objects.requireNonNull(description, "description is required");
    if (description != null && (description.isBlank() || description.length() > 2000))
      throw new BusinessRuleException("Invalid description");
    Objects.requireNonNull(reportStatus, "reportStatus is required");
    if (reportStatus != null && (reportStatus.isBlank() || reportStatus.length() > 20))
      throw new BusinessRuleException("Invalid reportStatus");
    if (!Set.of("OPEN", "ASSESSED", "CLOSED").contains(reportStatus))
      throw new BusinessRuleException("Invalid reportStatus");
    if (assessedCost != null
        && (assessedCost.signum() < 0 || assessedCost.scale() > 4 || assessedCost.precision() > 19))
      throw new BusinessRuleException("Invalid assessedCost");
    if ("ASSESSED".equals(reportStatus) && assessedCost == null)
      throw new BusinessRuleException("Assessed report requires cost");
    this.id = id;
    this.reservationId = reservationId;
    this.equipmentUnitId = equipmentUnitId;
    this.reportedAt = reportedAt;
    this.description = description;
    this.reportStatus = reportStatus;
    this.assessedCost = assessedCost;
  }

  public static DamageReport restore(
      DamageReportId id,
      ReservationId reservationId,
      EquipmentUnitId equipmentUnitId,
      Instant reportedAt,
      String description,
      String reportStatus,
      BigDecimal assessedCost) {
    return new DamageReport(
        id, reservationId, equipmentUnitId, reportedAt, description, reportStatus, assessedCost);
  }

  public static DamageReport register(
      DamageReportId id,
      ReservationId reservationId,
      EquipmentUnitId equipmentUnitId,
      Instant reportedAt,
      String description,
      String reportStatus,
      BigDecimal assessedCost,
      Clock clock) {
    var result =
        new DamageReport(
            id,
            reservationId,
            equipmentUnitId,
            reportedAt,
            description,
            reportStatus,
            assessedCost);
    result.recordEvent(new DamageReportChanged(id, Instant.now(clock)));
    return result;
  }

  public DamageReport assess(BigDecimal cost, Clock clock) {
    if (!"OPEN".equals(reportStatus))
      throw new BusinessRuleException("Only an open report can be assessed");
    Objects.requireNonNull(cost);
    var result =
        new DamageReport(
            id, reservationId, equipmentUnitId, reportedAt, description, "ASSESSED", cost);
    result.recordEvent(new DamageReportChanged(id, Instant.now(clock)));
    return result;
  }

  public DamageReportId id() {
    return id;
  }

  public ReservationId reservationId() {
    return reservationId;
  }

  public EquipmentUnitId equipmentUnitId() {
    return equipmentUnitId;
  }

  public Instant reportedAt() {
    return reportedAt;
  }

  public String description() {
    return description;
  }

  public String reportStatus() {
    return reportStatus;
  }

  public BigDecimal assessedCost() {
    return assessedCost;
  }

  public DamageReport transition(String next, Clock clock) {
    if (!(("OPEN".equals(reportStatus) && Set.of("ASSESSED", "CLOSED").contains(next))
        || ("ASSESSED".equals(reportStatus) && Set.of("CLOSED").contains(next))))
      throw new BusinessRuleException("Invalid DamageReport transition");
    var result =
        new DamageReport(
            id, reservationId, equipmentUnitId, reportedAt, description, next, assessedCost);
    result.recordEvent(new DamageReportChanged(id, Instant.now(clock)));
    return result;
  }
}
