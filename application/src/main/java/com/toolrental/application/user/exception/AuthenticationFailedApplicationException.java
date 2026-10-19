package com.toolrental.application.user.exception;

import com.toolrental.application.common.exception.ApplicationException;

public final class AuthenticationFailedApplicationException extends ApplicationException {
  public AuthenticationFailedApplicationException() {
    super("Invalid credentials or inactive account");
  }
}
