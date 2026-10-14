package com.toolrental.application.reservation.command;

import com.toolrental.domain.catalog.model.valueobject.UnidadEquipoId;
import com.toolrental.domain.common.model.valueobject.ClienteId;
import com.toolrental.domain.reservation.model.valueobject.PeriodoReserva;
import java.util.List;
import java.util.Objects;

public record SolicitarReservaCommand(
    ClienteId clienteId, PeriodoReserva periodo, List<UnidadEquipoId> unidades) {
  public SolicitarReservaCommand {
    Objects.requireNonNull(clienteId);
    Objects.requireNonNull(periodo);
    unidades = List.copyOf(unidades);
  }
}
