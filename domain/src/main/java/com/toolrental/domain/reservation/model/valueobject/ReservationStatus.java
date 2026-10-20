package com.toolrental.domain.reservation.model.valueobject;

public enum ReservationStatus {
  PENDING,
  PENDING_PAYMENT,
  CONFIRMED,
  REJECTED,
  CANCELLED,
  PAYMENT_FAILED,
  EXPIRED,
  CANCELLATION_REVIEW,
  NO_SHOW
}
