package com.toolrental.application.common.exception;

public final class AccessDeniedApplicationException extends ApplicationException {
  public AccessDeniedApplicationException() {
    super("Access denied");
  }
}
