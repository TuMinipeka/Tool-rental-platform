package com.toolrental.application.unitreturn.dto;

import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.unitreturn.model.aggregate.UnitReturn;
import java.time.Instant;
import java.util.UUID;

public record UnitReturnResponse(
    UUID reservationId,
    UUID equipmentUnitId,
    Instant receivedAt,
    String inboundCondition,
    String notes,
    int lateDays,
    Money lateFee) {
  public static UnitReturnResponse from(UnitReturn value) {
    return new UnitReturnResponse(
        value.id().reservationId().value(),
        value.id().equipmentUnitId().value(),
        value.receivedAt(),
        value.inboundCondition(),
        value.notes(),
        value.lateDays(),
        value.lateFee());
  }
}
