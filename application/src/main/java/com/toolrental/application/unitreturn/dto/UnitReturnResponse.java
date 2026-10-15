package com.toolrental.application.unitreturn.dto;

import com.toolrental.domain.unitreturn.model.aggregate.UnitReturn;
import java.time.Instant;
import java.util.UUID;

public record UnitReturnResponse(
    UUID reservationId,
    UUID equipmentUnitId,
    Instant receivedAt,
    String inboundCondition,
    String notes) {
  public static UnitReturnResponse from(UnitReturn value) {
    return new UnitReturnResponse(
        value.id().reservationId().value(),
        value.id().equipmentUnitId().value(),
        value.receivedAt(),
        value.inboundCondition(),
        value.notes());
  }
}
