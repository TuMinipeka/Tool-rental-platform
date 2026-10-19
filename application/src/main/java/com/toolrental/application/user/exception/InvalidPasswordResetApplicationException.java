package com.toolrental.application.user.exception;

import com.toolrental.application.common.exception.ApplicationException;

public final class InvalidPasswordResetApplicationException extends ApplicationException {
  public InvalidPasswordResetApplicationException() {
    super("Invalid or expired password recovery token");
  }
}
