package com.toolrental.application.reservation.usecase;

import com.toolrental.application.reservation.dto.ReservaResponse;
import com.toolrental.domain.common.exception.ReglaNegocioException;
import com.toolrental.domain.common.model.valueobject.ProveedorId;
import com.toolrental.domain.reservation.model.entity.DetalleReserva;
import com.toolrental.domain.reservation.model.valueobject.ReservaId;
import com.toolrental.domain.reservation.port.repository.GestionReservaAtomica;
import java.time.Clock;
import java.util.Objects;

public final class ConfirmarReservaUseCase {
  private final GestionReservaAtomica gestion;
  private final Clock clock;

  public ConfirmarReservaUseCase(GestionReservaAtomica gestion, Clock clock) {
    this.gestion = Objects.requireNonNull(gestion);
    this.clock = Objects.requireNonNull(clock);
  }

  public ReservaResponse execute(ReservaId id, ProveedorId actor) {
    Objects.requireNonNull(actor);
    return ReservaResponse.desde(
        gestion.ejecutar(
            Objects.requireNonNull(id),
            contexto -> {
              var r = contexto.reserva();
              if (!r.proveedorId().equals(actor) || !contexto.proveedorActivo(actor))
                throw new ReglaNegocioException("Proveedor no autorizado");
              if (!contexto.clienteActivo(r.clienteId()))
                throw new ReglaNegocioException("Cliente inactivo");
              var nuevas =
                  r.detalles().stream()
                      .map(
                          linea -> {
                            var unidad = contexto.unidad(linea.unidadId());
                            var herramienta = contexto.herramienta(unidad.herramientaId());
                            if (!unidad.operativa() || !herramienta.publicada())
                              throw new ReglaNegocioException("Equipo no alquilable");
                            if (!herramienta.proveedorId().equals(actor))
                              throw new ReglaNegocioException("Proveedor del equipo modificado");
                            if (!herramienta
                                .tarifaDiaria()
                                .moneda()
                                .equals(linea.tarifaDiaria().moneda()))
                              throw new ReglaNegocioException(
                                  "Moneda modificada; solicitar nueva reserva");
                            if (contexto.ocupada(unidad.id(), r.periodo()))
                              throw new ReglaNegocioException("Unidad no disponible");
                            return new DetalleReserva(
                                unidad.id(), actor, herramienta.tarifaDiaria());
                          })
                      .toList();
              return r.confirmarConTarifas(nuevas, clock);
            }));
  }
}
