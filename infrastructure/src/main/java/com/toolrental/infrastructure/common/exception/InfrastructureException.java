package com.toolrental.infrastructure.common.exception;

/** Base type for technical adapter failures; it does not represent business rules. */
public abstract class InfrastructureException extends RuntimeException {
  protected InfrastructureException(String message) {
    super(message);
  }

  protected InfrastructureException(String message, Throwable cause) {
    super(message, cause);
  }
}
