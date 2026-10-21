package com.toolrental.application.supplier.exception;

import com.toolrental.application.common.exception.ApplicationException;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;

public final class SupplierNotFoundApplicationException extends ApplicationException {
  public SupplierNotFoundApplicationException() {
    super("Supplier not found");
  }

  public SupplierNotFoundApplicationException(SupplierId id) {
    super("Supplier not found: " + id.value());
  }

  public SupplierNotFoundApplicationException(SupplierId id, Throwable cause) {
    super("Supplier not found: " + id.value(), cause);
  }
}
