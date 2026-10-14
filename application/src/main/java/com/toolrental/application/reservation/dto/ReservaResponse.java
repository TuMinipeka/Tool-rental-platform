package com.toolrental.application.reservation.dto;

import com.toolrental.domain.common.model.valueobject.ClienteId;
import com.toolrental.domain.common.model.valueobject.Dinero;
import com.toolrental.domain.common.model.valueobject.ProveedorId;
import com.toolrental.domain.reservation.model.aggregate.Reserva;
import com.toolrental.domain.reservation.model.entity.DetalleReserva;
import com.toolrental.domain.reservation.model.valueobject.EstadoReserva;
import com.toolrental.domain.reservation.model.valueobject.PeriodoReserva;
import com.toolrental.domain.reservation.model.valueobject.ReservaId;
import java.util.List;

public record ReservaResponse(
    ReservaId id,
    ClienteId clienteId,
    ProveedorId proveedorId,
    PeriodoReserva periodo,
    EstadoReserva estado,
    List<DetalleReserva> detalles,
    Dinero total,
    java.time.Instant creadaEn) {
  public ReservaResponse {
    detalles = List.copyOf(detalles);
  }

  public static ReservaResponse desde(Reserva r) {
    return new ReservaResponse(
        r.id(),
        r.clienteId(),
        r.proveedorId(),
        r.periodo(),
        r.estado(),
        r.detalles(),
        r.total(),
        r.creadaEn());
  }
}
