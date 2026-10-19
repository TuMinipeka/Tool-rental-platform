package com.toolrental.application.usersession.exception;

import com.toolrental.application.common.exception.ApplicationException;

public final class InvalidSessionApplicationException extends ApplicationException {
  public InvalidSessionApplicationException() {
    super("Session is invalid or expired");
  }
}
