package com.toolrental.application.damagereport.dto;

import com.toolrental.domain.damagereport.model.aggregate.DamageReport;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DamageReportResponse(
    UUID damageReportId,
    UUID reservationId,
    UUID equipmentUnitId,
    Instant reportedAt,
    String description,
    String reportStatus,
    BigDecimal assessedCost) {
  public static DamageReportResponse from(DamageReport value) {
    return new DamageReportResponse(
        value.id().value(),
        value.reservationId().value(),
        value.equipmentUnitId().value(),
        value.reportedAt(),
        value.description(),
        value.reportStatus(),
        value.assessedCost());
  }
}
