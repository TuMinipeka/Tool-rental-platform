package com.toolrental.application.customer.exception;

import com.toolrental.application.common.exception.ApplicationException;

public final class CustomerNotFoundApplicationException extends ApplicationException {
  public CustomerNotFoundApplicationException() {
    super("Customer not found");
  }
}
