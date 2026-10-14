package com.toolrental.application.reservation.exception;

import com.toolrental.application.common.exception.ApplicationException;
import com.toolrental.domain.reservation.model.valueobject.ReservaId;
import java.util.Objects;

public final class ReservaNotFoundApplicationException extends ApplicationException {
  public ReservaNotFoundApplicationException(ReservaId id) {
    super("Reserva no encontrada: " + Objects.requireNonNull(id).value());
  }

  public ReservaNotFoundApplicationException(ReservaId id, Throwable cause) {
    super("Reserva no encontrada: " + Objects.requireNonNull(id).value(), cause);
  }
}
