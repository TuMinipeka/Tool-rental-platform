package com.toolrental.domain.reservation.model.aggregate;

import com.toolrental.domain.common.exception.ReglaNegocioException;
import com.toolrental.domain.common.model.valueobject.ClienteId;
import com.toolrental.domain.common.model.valueobject.Dinero;
import com.toolrental.domain.common.model.valueobject.ProveedorId;
import com.toolrental.domain.reservation.event.ReservaConfirmada;
import com.toolrental.domain.reservation.model.entity.DetalleReserva;
import com.toolrental.domain.reservation.model.valueobject.EstadoReserva;
import com.toolrental.domain.reservation.model.valueobject.PeriodoReserva;
import com.toolrental.domain.reservation.model.valueobject.ReservaId;
import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Reserva {
  private final ReservaId id;
  private final ClienteId clienteId;
  private final PeriodoReserva periodo;
  private final List<DetalleReserva> detalles;
  private final Instant creadaEn;
  private EstadoReserva estado = EstadoReserva.PENDIENTE;
  private ReservaConfirmada confirmacion;

  private Reserva(
      ClienteId cliente, PeriodoReserva periodo, List<DetalleReserva> detalles, Clock clock) {
    id = ReservaId.generar();
    clienteId = Objects.requireNonNull(cliente);
    this.periodo = Objects.requireNonNull(periodo);
    this.detalles = validar(detalles);
    creadaEn = Instant.now(clock);
  }

  private static List<DetalleReserva> validar(List<DetalleReserva> lineas) {
    var copia = List.copyOf(lineas);
    if (copia.isEmpty()) throw new ReglaNegocioException("Reserva sin unidades");
    var primera = copia.getFirst();
    var unidades = new HashSet<>();
    for (var linea : copia) {
      if (!unidades.add(linea.unidadId())) throw new ReglaNegocioException("Unidad duplicada");
      if (!primera.proveedorId().equals(linea.proveedorId()))
        throw new ReglaNegocioException("Proveedores mezclados");
      if (!primera.tarifaDiaria().moneda().equals(linea.tarifaDiaria().moneda()))
        throw new ReglaNegocioException("Monedas mezcladas");
    }
    return copia;
  }

  public static Reserva solicitar(
      ClienteId cliente, PeriodoReserva periodo, List<DetalleReserva> lineas, Clock clock) {
    return new Reserva(cliente, periodo, lineas, clock);
  }

  /** Devuelve la versión confirmada; el puerto atómico debe reemplazar la versión pendiente. */
  public Reserva confirmarConTarifas(List<DetalleReserva> tarifasVigentes, Clock clock) {
    exigirPendiente();
    var nuevas = validar(tarifasVigentes);
    var ids =
        detalles.stream()
            .map(DetalleReserva::unidadId)
            .collect(java.util.stream.Collectors.toSet());
    if (!ids.equals(
            nuevas.stream()
                .map(DetalleReserva::unidadId)
                .collect(java.util.stream.Collectors.toSet()))
        || !proveedorId().equals(nuevas.getFirst().proveedorId()))
      throw new ReglaNegocioException("La confirmación no puede cambiar unidades ni proveedor");
    if (!detalles
        .getFirst()
        .tarifaDiaria()
        .moneda()
        .equals(nuevas.getFirst().tarifaDiaria().moneda()))
      throw new ReglaNegocioException("La confirmación no puede cambiar la moneda");
    return new Reserva(
        this, nuevas, EstadoReserva.CONFIRMADA, new ReservaConfirmada(id, Instant.now(clock)));
  }

  private Reserva(
      Reserva original,
      List<DetalleReserva> nuevas,
      EstadoReserva estado,
      ReservaConfirmada confirmacion) {
    id = original.id;
    clienteId = original.clienteId;
    periodo = original.periodo;
    creadaEn = original.creadaEn;
    detalles = nuevas;
    this.estado = estado;
    this.confirmacion = confirmacion;
  }

  public Reserva copia() {
    return new Reserva(this, detalles, estado, confirmacion);
  }

  public void rechazar() {
    exigirPendiente();
    estado = EstadoReserva.RECHAZADA;
  }

  public void cancelar() {
    if (estado != EstadoReserva.PENDIENTE && estado != EstadoReserva.CONFIRMADA)
      throw new ReglaNegocioException("Reserva terminal");
    estado = EstadoReserva.CANCELADA;
  }

  private void exigirPendiente() {
    if (estado != EstadoReserva.PENDIENTE)
      throw new ReglaNegocioException("La reserva no está pendiente");
  }

  public Dinero total() {
    var suma = new Dinero(java.math.BigDecimal.ZERO, detalles.getFirst().tarifaDiaria().moneda());
    for (var linea : detalles)
      suma = suma.sumar(linea.tarifaDiaria().multiplicar(periodo.diasFacturables()));
    return suma;
  }

  public ReservaId id() {
    return id;
  }

  public ClienteId clienteId() {
    return clienteId;
  }

  public ProveedorId proveedorId() {
    return detalles.getFirst().proveedorId();
  }

  public PeriodoReserva periodo() {
    return periodo;
  }

  public List<DetalleReserva> detalles() {
    return detalles;
  }

  public EstadoReserva estado() {
    return estado;
  }

  public Instant creadaEn() {
    return creadaEn;
  }

  public Optional<ReservaConfirmada> confirmacion() {
    return Optional.ofNullable(confirmacion);
  }
}
