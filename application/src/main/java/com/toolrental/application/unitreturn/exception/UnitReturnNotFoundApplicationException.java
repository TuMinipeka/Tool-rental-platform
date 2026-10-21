package com.toolrental.application.unitreturn.exception;

import com.toolrental.application.common.exception.ApplicationException;

public final class UnitReturnNotFoundApplicationException extends ApplicationException {
  public UnitReturnNotFoundApplicationException() {
    super("UnitReturn not found");
  }
}
