package com.toolrental.application.user.exception;

import com.toolrental.application.common.exception.ApplicationException;

public final class UserNotFoundApplicationException extends ApplicationException {
  public UserNotFoundApplicationException() {
    super("User not found");
  }
}
