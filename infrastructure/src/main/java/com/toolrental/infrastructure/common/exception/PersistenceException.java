package com.toolrental.infrastructure.common.exception;

/** Base error contract for persistence adapter failures. */
public final class PersistenceException extends InfrastructureException {
  public PersistenceException(String message, Throwable cause) {
    super(message, cause);
  }
}
