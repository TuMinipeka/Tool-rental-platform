package com.toolrental.application.catalog.exception;

import com.toolrental.application.common.exception.ApplicationException;
import com.toolrental.domain.catalog.model.valueobject.HerramientaId;
import java.util.Objects;

public final class HerramientaNotFoundApplicationException extends ApplicationException {
  public HerramientaNotFoundApplicationException(HerramientaId id) {
    super("Herramienta no encontrada: " + Objects.requireNonNull(id).value());
  }

  public HerramientaNotFoundApplicationException(HerramientaId id, Throwable cause) {
    super("Herramienta no encontrada: " + Objects.requireNonNull(id).value(), cause);
  }
}
