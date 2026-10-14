package com.toolrental.domain.common.model.valueobject;

import com.toolrental.domain.common.exception.ReglaNegocioException;

public final class Validacion {
  private Validacion() {}

  public static String texto(String value, String field) {
    if (value == null || value.isBlank())
      throw new ReglaNegocioException(field + " es obligatorio");
    return value.strip();
  }
}
