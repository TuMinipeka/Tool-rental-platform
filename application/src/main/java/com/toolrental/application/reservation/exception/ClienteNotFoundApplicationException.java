package com.toolrental.application.reservation.exception;

import com.toolrental.application.common.exception.ApplicationException;
import com.toolrental.domain.common.model.valueobject.ClienteId;
import java.util.Objects;

public final class ClienteNotFoundApplicationException extends ApplicationException {
  public ClienteNotFoundApplicationException(ClienteId id) {
    super("Cliente no encontrado o inactivo: " + Objects.requireNonNull(id).value());
  }

  public ClienteNotFoundApplicationException(ClienteId id, Throwable cause) {
    super("Cliente no encontrado o inactivo: " + Objects.requireNonNull(id).value(), cause);
  }
}
