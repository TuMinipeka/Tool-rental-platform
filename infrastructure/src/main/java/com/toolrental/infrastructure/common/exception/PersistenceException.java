package com.toolrental.infrastructure.common.exception;

/** Error contract for the future JPA adapter; persistence is not implemented yet. */
public final class PersistenceException extends InfrastructureException {
  public PersistenceException(String message, Throwable cause) {
    super(message, cause);
  }
}
