package com.toolrental.application.rental.exception;

import com.toolrental.application.common.exception.ApplicationException;

public final class RentalNotFoundApplicationException extends ApplicationException {
  public RentalNotFoundApplicationException() {
    super("Rental not found");
  }
}
