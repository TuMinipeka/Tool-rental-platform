package com.toolrental.domain.common.model.valueobject;

import com.toolrental.domain.common.exception.ReglaNegocioException;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public record Dinero(BigDecimal importe, Currency moneda) {
  public Dinero {
    Objects.requireNonNull(importe, "importe");
    Objects.requireNonNull(moneda, "moneda");
    if (importe.signum() < 0) throw new ReglaNegocioException("El importe no puede ser negativo");
    importe = importe.stripTrailingZeros();
  }

  public Dinero multiplicar(long cantidad) {
    if (cantidad < 0) throw new ReglaNegocioException("Cantidad negativa");
    return new Dinero(importe.multiply(BigDecimal.valueOf(cantidad)), moneda);
  }

  public Dinero sumar(Dinero otro) {
    if (!moneda.equals(otro.moneda)) throw new ReglaNegocioException("Monedas incompatibles");
    return new Dinero(importe.add(otro.importe), moneda);
  }
}
