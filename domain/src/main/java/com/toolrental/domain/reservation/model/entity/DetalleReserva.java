package com.toolrental.domain.reservation.model.entity;

import com.toolrental.domain.catalog.model.valueobject.UnidadEquipoId;
import com.toolrental.domain.common.model.valueobject.Dinero;
import com.toolrental.domain.common.model.valueobject.ProveedorId;
import java.util.Objects;

/** Identidad local: unidadId dentro de una Reserva. Tarifa es una instantánea. */
public record DetalleReserva(
    UnidadEquipoId unidadId, ProveedorId proveedorId, Dinero tarifaDiaria) {
  public DetalleReserva {
    Objects.requireNonNull(unidadId);
    Objects.requireNonNull(proveedorId);
    Objects.requireNonNull(tarifaDiaria);
  }
}
