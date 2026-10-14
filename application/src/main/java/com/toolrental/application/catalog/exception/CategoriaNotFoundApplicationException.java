package com.toolrental.application.catalog.exception;

import com.toolrental.application.common.exception.ApplicationException;
import com.toolrental.domain.catalog.model.valueobject.CategoriaId;
import java.util.Objects;

public final class CategoriaNotFoundApplicationException extends ApplicationException {
  public CategoriaNotFoundApplicationException(CategoriaId id) {
    super("Categoría no encontrada: " + Objects.requireNonNull(id).value());
  }

  public CategoriaNotFoundApplicationException(CategoriaId id, Throwable cause) {
    super("Categoría no encontrada: " + Objects.requireNonNull(id).value(), cause);
  }
}
