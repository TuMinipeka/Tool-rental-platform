package com.toolrental.application.catalog.exception;

import com.toolrental.application.common.exception.ApplicationException;
import com.toolrental.domain.catalog.model.valueobject.UnidadEquipoId;
import java.util.Objects;

public final class UnidadEquipoNotFoundApplicationException extends ApplicationException {
  public UnidadEquipoNotFoundApplicationException(UnidadEquipoId id) {
    super("Unidad de equipo no encontrada: " + Objects.requireNonNull(id).value());
  }

  public UnidadEquipoNotFoundApplicationException(UnidadEquipoId id, Throwable cause) {
    super("Unidad de equipo no encontrada: " + Objects.requireNonNull(id).value(), cause);
  }
}
