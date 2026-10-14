package com.toolrental.infrastructure.common.exception;

/** Contrato de error para el futuro adaptador JPA; no implica persistencia implementada. */
public final class PersistenceException extends InfrastructureException {
  public PersistenceException(String message, Throwable cause) {
    super(message, cause);
  }
}
