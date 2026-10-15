package com.toolrental.application.reservationitem.dto;

import com.toolrental.domain.reservationitem.model.aggregate.ReservationItem;
import java.math.BigDecimal;
import java.util.UUID;

public record ReservationItemResponse(
    UUID reservationId, UUID equipmentUnitId, BigDecimal agreedDailyRate) {
  public static ReservationItemResponse from(ReservationItem value) {
    return new ReservationItemResponse(
        value.id().reservationId().value(),
        value.id().equipmentUnitId().value(),
        value.agreedDailyRate());
  }
}
