package com.toolrental.application.reservationtermination.dto;

import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservationtermination.model.aggregate.ReservationTermination;
import com.toolrental.domain.reservationtermination.model.valueobject.ReservationTerminationId;
import java.time.Instant;

public record ReservationTerminationResponse(
    ReservationTerminationId id,
    ReservationId reservationId,
    String type,
    String status,
    Money fee,
    Money refund,
    Money outstanding,
    String reason,
    Instant requestedAt,
    Instant resolvedAt) {
  public static ReservationTerminationResponse from(ReservationTermination value) {
    return new ReservationTerminationResponse(
        value.id(),
        value.reservationId(),
        value.type(),
        value.status(),
        value.fee(),
        value.refund(),
        value.outstanding(),
        value.reason(),
        value.requestedAt(),
        value.resolvedAt());
  }
}
