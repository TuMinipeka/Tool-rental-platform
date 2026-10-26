package com.toolrental.application.reservation.dto;

import com.toolrental.domain.cancellationpolicy.model.valueobject.CancellationPolicyId;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.customer.model.valueobject.CustomerId;
import com.toolrental.domain.reservation.model.aggregate.Reservation;
import com.toolrental.domain.reservation.model.valueobject.DeliveryAddress;
import com.toolrental.domain.reservation.model.valueobject.FulfillmentMethod;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import com.toolrental.domain.reservationitem.model.entity.ReservationLine;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ReservationResponse(
    ReservationId id,
    CustomerId customerId,
    SupplierId supplierId,
    ReservationPeriod period,
    ReservationStatus status,
    List<ReservationLine> items,
    Money rentalSubtotal,
    Money deliveryFee,
    Money total,
    Instant scheduledFulfillmentAt,
    FulfillmentMethod fulfillmentMethod,
    DeliveryAddress deliveryAddress,
    CancellationPolicyId cancellationPolicyId,
    LocalDate effectiveReturnDate,
    List<String> allowedActions,
    Instant expiresAt,
    Instant createdAt) {
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
        r.rentalSubtotal(),
        r.deliveryFee(),
        r.total(),
        r.scheduledFulfillmentAt(),
        r.fulfillmentMethod(),
        r.deliveryAddress().orElse(null),
        r.cancellationPolicyId(),
        r.effectiveReturnDate(),
        allowedActions(r),
        r.expiresAt(),
        r.createdAt());
  }

  private static List<String> allowedActions(Reservation reservation) {
    return switch (reservation.status()) {
      case PENDING -> List.of("CANCEL");
      case PENDING_PAYMENT -> List.of("PAY", "CANCEL");
      case CONFIRMED -> List.of("CANCEL", "REQUEST_WITHDRAWAL", "REQUEST_EXTENSION");
      case CANCELLATION_REVIEW -> List.of();
      case REJECTED, CANCELLED, PAYMENT_FAILED, EXPIRED, NO_SHOW -> List.of();
      case PENDING_RETURN -> throw new UnsupportedOperationException("Unimplemented case: " + reservation.status());
      case RETURN_OK -> throw new UnsupportedOperationException("Unimplemented case: " + reservation.status());
      case RETURN_WITH_DAMAGE -> throw new UnsupportedOperationException("Unimplemented case: " + reservation.status());
      default -> throw new IllegalArgumentException("Unexpected value: " + reservation.status());
    };
  }
}
