package com.toolrental.application.reservation.usecase;

import com.toolrental.application.catalog.exception.HerramientaNotFoundApplicationException;
import com.toolrental.application.catalog.exception.ProveedorNotFoundApplicationException;
import com.toolrental.application.catalog.exception.UnidadEquipoNotFoundApplicationException;
import com.toolrental.application.reservation.command.SolicitarReservaCommand;
import com.toolrental.application.reservation.dto.ReservaResponse;
import com.toolrental.application.reservation.exception.ClienteNotFoundApplicationException;
import com.toolrental.domain.catalog.port.repository.HerramientaRepository;
import com.toolrental.domain.catalog.port.repository.UnidadEquipoRepository;
import com.toolrental.domain.common.exception.ReglaNegocioException;
import com.toolrental.domain.common.port.ParticipantesPort;
import com.toolrental.domain.reservation.model.aggregate.Reserva;
import com.toolrental.domain.reservation.model.entity.DetalleReserva;
import com.toolrental.domain.reservation.port.repository.ReservaRepository;
import java.time.Clock;
import java.util.Objects;

public final class SolicitarReservaUseCase {
  private final ReservaRepository reservas;
  private final UnidadEquipoRepository unidades;
  private final HerramientaRepository herramientas;
  private final ParticipantesPort participantes;
  private final Clock clock;

  public SolicitarReservaUseCase(
      ReservaRepository r,
      UnidadEquipoRepository u,
      HerramientaRepository h,
      ParticipantesPort p,
      Clock clock) {
    reservas = Objects.requireNonNull(r);
    unidades = Objects.requireNonNull(u);
    herramientas = Objects.requireNonNull(h);
    participantes = Objects.requireNonNull(p);
    this.clock = Objects.requireNonNull(clock);
  }

  public ReservaResponse execute(SolicitarReservaCommand c) {
    if (!participantes.clienteActivo(c.clienteId()))
      throw new ClienteNotFoundApplicationException(c.clienteId());
    var lineas =
        c.unidades().stream()
            .map(
                id -> {
                  var u =
                      unidades
                          .buscar(id)
                          .orElseThrow(() -> new UnidadEquipoNotFoundApplicationException(id));
                  var h =
                      herramientas
                          .buscar(u.herramientaId())
                          .orElseThrow(
                              () -> new HerramientaNotFoundApplicationException(u.herramientaId()));
                  if (!u.operativa() || !h.publicada())
                    throw new ReglaNegocioException("Equipo no alquilable");
                  if (!participantes.proveedorActivo(h.proveedorId()))
                    throw new ProveedorNotFoundApplicationException(h.proveedorId());
                  return new DetalleReserva(id, h.proveedorId(), h.tarifaDiaria());
                })
            .toList();
    var reserva = Reserva.solicitar(c.clienteId(), c.periodo(), lineas, clock);
    reservas.agregarSolicitud(reserva);
    return ReservaResponse.desde(reserva);
  }
}
