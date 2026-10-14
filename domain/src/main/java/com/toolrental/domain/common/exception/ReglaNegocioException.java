package com.toolrental.domain.common.exception;

public class ReglaNegocioException extends DomainException {
  public ReglaNegocioException(String message) {
    super(message);
  }

  public ReglaNegocioException(String message, Throwable cause) {
    super(message, cause);
  }
}
