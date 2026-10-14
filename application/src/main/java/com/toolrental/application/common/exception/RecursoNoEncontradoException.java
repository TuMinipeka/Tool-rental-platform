package com.toolrental.application.common.exception;

public class RecursoNoEncontradoException extends RuntimeException {
  public RecursoNoEncontradoException(String recurso) {
    super(recurso + " no encontrado");
  }
}
