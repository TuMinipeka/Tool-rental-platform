package com.toolrental.application.catalog.exception;

import com.toolrental.application.common.exception.ApplicationException;
import com.toolrental.domain.common.model.valueobject.ProveedorId;
import java.util.Objects;

public final class ProveedorNotFoundApplicationException extends ApplicationException {
  public ProveedorNotFoundApplicationException(ProveedorId id) {
    super("Proveedor no encontrado o inactivo: " + Objects.requireNonNull(id).value());
  }

  public ProveedorNotFoundApplicationException(ProveedorId id, Throwable cause) {
    super("Proveedor no encontrado o inactivo: " + Objects.requireNonNull(id).value(), cause);
  }
}
