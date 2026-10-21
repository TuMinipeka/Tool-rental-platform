package com.toolrental.application.userrole.exception;

import com.toolrental.application.common.exception.ApplicationException;

public final class UserRoleNotFoundApplicationException extends ApplicationException {
  public UserRoleNotFoundApplicationException() {
    super("UserRole not found");
  }
}
