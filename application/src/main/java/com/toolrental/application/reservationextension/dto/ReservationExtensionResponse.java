package com.toolrental.application.reservationextension.dto;

import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservationextension.model.aggregate.ReservationExtension;
import com.toolrental.domain.reservationextension.model.valueobject.ReservationExtensionId;
import java.time.Instant;
import java.time.LocalDate;

public record ReservationExtensionResponse(
    ReservationExtensionId id,
    ReservationId reservationId,
    LocalDate requestedReturnDate,
    String status,
    Money additionalAmount,
    Instant requestedAt,
    Instant resolvedAt) {
  public static ReservationExtensionResponse from(ReservationExtension value) {
    return new ReservationExtensionResponse(
        value.id(),
        value.reservationId(),
        value.requestedReturnDate(),
        value.status(),
        value.additionalAmount(),
        value.requestedAt(),
        value.resolvedAt());
  }
}
