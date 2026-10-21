package com.toolrental.application.unitdelivery.exception;

import com.toolrental.application.common.exception.ApplicationException;

public final class UnitDeliveryNotFoundApplicationException extends ApplicationException {
  public UnitDeliveryNotFoundApplicationException() {
    super("UnitDelivery not found");
  }
}
