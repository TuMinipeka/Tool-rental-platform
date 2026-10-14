package com.toolrental.domain.reservation.model.valueobject;

import com.toolrental.domain.common.exception.ReglaNegocioException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/** Ambas fechas son inclusivas para facturación y disponibilidad. */
public record PeriodoReserva(LocalDate inicio, LocalDate devolucion) {
  public PeriodoReserva {
    Objects.requireNonNull(inicio, "inicio");
    Objects.requireNonNull(devolucion, "devolucion");
    if (devolucion.isBefore(inicio))
      throw new ReglaNegocioException("La devolución es anterior al inicio");
  }

  public long diasFacturables() {
    return ChronoUnit.DAYS.between(inicio, devolucion) + 1;
  }

  public boolean solapa(PeriodoReserva otro) {
    return !devolucion.isBefore(otro.inicio) && !otro.devolucion.isBefore(inicio);
  }
}
