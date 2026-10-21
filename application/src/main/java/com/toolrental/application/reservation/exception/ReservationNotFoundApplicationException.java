package com.toolrental.application.reservation.exception;

import com.toolrental.application.common.exception.ApplicationException;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import java.util.Objects;

public final class ReservationNotFoundApplicationException extends ApplicationException {
  public ReservationNotFoundApplicationException(ReservationId id) {
    super("Reservation not found: " + Objects.requireNonNull(id).value());
  }

  public ReservationNotFoundApplicationException(ReservationId id, Throwable cause) {
    super("Reservation not found: " + Objects.requireNonNull(id).value(), cause);
  }
}
