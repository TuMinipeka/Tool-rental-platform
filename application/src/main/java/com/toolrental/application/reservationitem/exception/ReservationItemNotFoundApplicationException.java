package com.toolrental.application.reservationitem.exception;

import com.toolrental.application.common.exception.ApplicationException;

public final class ReservationItemNotFoundApplicationException extends ApplicationException {
  public ReservationItemNotFoundApplicationException() {
    super("ReservationItem not found");
  }
}
