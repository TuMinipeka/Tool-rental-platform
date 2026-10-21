package com.toolrental.application.role.exception;

import com.toolrental.application.common.exception.ApplicationException;

public final class RoleNotFoundApplicationException extends ApplicationException {
  public RoleNotFoundApplicationException() {
    super("Role not found");
  }
}
