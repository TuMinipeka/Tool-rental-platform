package com.toolrental.domain.reservation.model.valueobject;

public enum ReservationStatus {
  PENDING,
  PENDING_PAYMENT,
  CONFIRMED,
  REJECTED,
  CANCELLED,
  PAYMENT_FAILED,
  EXPIRED,
  PENDING_RETURN,
  RETURN_OK,
  DAMAGE,
  OK,
  RETURN_WITH_DAMAGE,
  CANCELLATION_REVIEW,
  NO_SHOW
}
