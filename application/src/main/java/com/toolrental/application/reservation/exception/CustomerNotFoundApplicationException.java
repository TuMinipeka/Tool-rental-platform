package com.toolrental.application.reservation.exception;

import com.toolrental.application.common.exception.ApplicationException;
import com.toolrental.domain.common.model.valueobject.CustomerId;
import java.util.Objects;

public final class CustomerNotFoundApplicationException extends ApplicationException {
  public CustomerNotFoundApplicationException(CustomerId id) {
    super("Customer not found or inactive: " + Objects.requireNonNull(id).value());
  }

  public CustomerNotFoundApplicationException(CustomerId id, Throwable cause) {
    super("Customer not found or inactive: " + Objects.requireNonNull(id).value(), cause);
  }
}
