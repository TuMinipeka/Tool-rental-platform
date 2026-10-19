package com.toolrental.application.usersession.exception;

import com.toolrental.application.common.exception.ApplicationException;

public final class RefreshTokenReuseApplicationException extends ApplicationException {
  public RefreshTokenReuseApplicationException() {
    super("Refresh token reuse was detected");
  }
}
