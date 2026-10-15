package com.toolrental.application.reservation.dto;

import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.customer.model.valueobject.CustomerId;
import com.toolrental.domain.reservation.model.aggregate.Reservation;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import com.toolrental.domain.reservationitem.model.entity.ReservationLine;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import java.util.List;

public record ReservationResponse(
    ReservationId id,
    CustomerId customerId,
    SupplierId supplierId,
    ReservationPeriod period,
    ReservationStatus status,
    List<ReservationLine> items,
    Money total,
    java.time.Instant createdAt) {
  public ReservationResponse {
    items = List.copyOf(items);
  }

  public static ReservationResponse from(Reservation r) {
    return new ReservationResponse(
        r.id(),
        r.customerId(),
        r.supplierId(),
        r.period(),
        r.status(),
        r.items(),
        r.total(),
        r.createdAt());
  }
}
