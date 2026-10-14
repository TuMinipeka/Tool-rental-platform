package com.toolrental.infrastructure.common.exception;

/** Base para fallos técnicos de los futuros adaptadores; no representa reglas del negocio. */
public abstract class InfrastructureException extends RuntimeException {
  protected InfrastructureException(String message) {
    super(message);
  }

  protected InfrastructureException(String message, Throwable cause) {
    super(message, cause);
  }
}
