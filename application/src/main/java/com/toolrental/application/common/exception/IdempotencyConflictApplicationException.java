package com.toolrental.application.common.exception;

public final class IdempotencyConflictApplicationException extends ApplicationException {
  public IdempotencyConflictApplicationException() {
    super("Idempotency key was already used for a different operation");
  }
}
