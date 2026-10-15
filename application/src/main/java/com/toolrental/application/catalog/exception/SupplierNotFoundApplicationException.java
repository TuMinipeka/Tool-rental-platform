package com.toolrental.application.catalog.exception;

import com.toolrental.application.common.exception.ApplicationException;
import com.toolrental.domain.common.model.valueobject.SupplierId;
import java.util.Objects;

public final class SupplierNotFoundApplicationException extends ApplicationException {
  public SupplierNotFoundApplicationException(SupplierId id) {
    super("Supplier not found or inactive: " + Objects.requireNonNull(id).value());
  }

  public SupplierNotFoundApplicationException(SupplierId id, Throwable cause) {
    super("Supplier not found or inactive: " + Objects.requireNonNull(id).value(), cause);
  }
}
