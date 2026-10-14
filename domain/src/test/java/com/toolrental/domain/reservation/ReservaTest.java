package com.toolrental.domain.reservation;

import static org.junit.jupiter.api.Assertions.*;

import com.toolrental.domain.catalog.model.aggregate.Categoria;
import com.toolrental.domain.catalog.model.aggregate.Herramienta;
import com.toolrental.domain.catalog.model.aggregate.UnidadEquipo;
import com.toolrental.domain.catalog.model.valueobject.CategoriaId;
import com.toolrental.domain.catalog.model.valueobject.HerramientaId;
import com.toolrental.domain.catalog.model.valueobject.UnidadEquipoId;
import com.toolrental.domain.common.exception.ReglaNegocioException;
import com.toolrental.domain.common.model.valueobject.ClienteId;
import com.toolrental.domain.common.model.valueobject.Dinero;
import com.toolrental.domain.common.model.valueobject.ProveedorId;
import com.toolrental.domain.reservation.model.aggregate.Reserva;
import com.toolrental.domain.reservation.model.entity.DetalleReserva;
import com.toolrental.domain.reservation.model.valueobject.EstadoReserva;
import com.toolrental.domain.reservation.model.valueobject.PeriodoReserva;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ReservaTest {
  private final Clock clock = Clock.fixed(Instant.parse("2026-10-14T12:00:00Z"), ZoneOffset.UTC);
  private final ProveedorId proveedor = ProveedorId.generar();
  private final ClienteId cliente = ClienteId.generar();
  private final UnidadEquipoId unidad = UnidadEquipoId.generar();
  private final LocalDate fecha = LocalDate.of(2026, 10, 14);

  private Dinero dinero(String valor) {
    return new Dinero(new BigDecimal(valor), Currency.getInstance("COP"));
  }

  private DetalleReserva linea() {
    return new DetalleReserva(unidad, proveedor, dinero("10000"));
  }

  private Reserva pendiente() {
    return Reserva.solicitar(cliente, new PeriodoReserva(fecha, fecha), List.of(linea()), clock);
  }

  @Test
  void calculaDiasInclusivosYTotalExacto() {
    assertEquals(1, new PeriodoReserva(fecha, fecha).diasFacturables());
    assertEquals(2, new PeriodoReserva(fecha, fecha.plusDays(1)).diasFacturables());
    var r =
        Reserva.solicitar(
            cliente, new PeriodoReserva(fecha, fecha.plusDays(1)), List.of(linea()), clock);
    assertEquals(dinero("20000"), r.total());
    assertEquals(clock.instant(), r.creadaEn());
  }

  @Test
  void detectaSolapamientoEnAmbosExtremos() {
    var p = new PeriodoReserva(fecha, fecha.plusDays(1));
    assertTrue(p.solapa(new PeriodoReserva(fecha.plusDays(1), fecha.plusDays(2))));
    assertTrue(p.solapa(new PeriodoReserva(fecha.minusDays(1), fecha)));
    assertTrue(p.solapa(p));
    assertFalse(p.solapa(new PeriodoReserva(fecha.plusDays(2), fecha.plusDays(3))));
  }

  @Test
  void rechazaFechasInvalidasYReservasVacias() {
    assertThrows(ReglaNegocioException.class, () -> new PeriodoReserva(fecha, fecha.minusDays(1)));
    assertThrows(
        ReglaNegocioException.class,
        () -> Reserva.solicitar(cliente, new PeriodoReserva(fecha, fecha), List.of(), clock));
  }

  @Test
  void rechazaDuplicadosProveedoresYMonedasMezcladas() {
    var p = new PeriodoReserva(fecha, fecha);
    assertThrows(
        ReglaNegocioException.class,
        () -> Reserva.solicitar(cliente, p, List.of(linea(), linea()), clock));
    var otro = new DetalleReserva(UnidadEquipoId.generar(), ProveedorId.generar(), dinero("1"));
    assertThrows(
        ReglaNegocioException.class,
        () -> Reserva.solicitar(cliente, p, List.of(linea(), otro), clock));
    var usd =
        new DetalleReserva(
            UnidadEquipoId.generar(),
            proveedor,
            new Dinero(BigDecimal.ONE, Currency.getInstance("USD")));
    assertThrows(
        ReglaNegocioException.class,
        () -> Reserva.solicitar(cliente, p, List.of(linea(), usd), clock));
  }

  @Test
  void confirmarCongelaTarifasSinModificarSolicitudOriginal() {
    var original = pendiente();
    var nuevas = new ArrayList<>(List.of(new DetalleReserva(unidad, proveedor, dinero("15000"))));
    var confirmada = original.confirmarConTarifas(nuevas, clock);
    nuevas.clear();
    assertEquals(EstadoReserva.PENDIENTE, original.estado());
    assertEquals(EstadoReserva.CONFIRMADA, confirmada.estado());
    assertEquals(original.id(), confirmada.id());
    assertEquals(dinero("15000"), confirmada.total());
    assertEquals(clock.instant(), confirmada.confirmacion().orElseThrow().ocurridaEn());
    assertThrows(UnsupportedOperationException.class, () -> confirmada.detalles().clear());
  }

  @Test
  void confirmarNoPuedeCambiarUnidadesProveedorNiMoneda() {
    var r = pendiente();
    assertThrows(
        ReglaNegocioException.class,
        () ->
            r.confirmarConTarifas(
                List.of(new DetalleReserva(UnidadEquipoId.generar(), proveedor, dinero("1"))),
                clock));
    assertThrows(
        ReglaNegocioException.class,
        () ->
            r.confirmarConTarifas(
                List.of(new DetalleReserva(unidad, ProveedorId.generar(), dinero("1"))), clock));
    assertThrows(
        ReglaNegocioException.class,
        () ->
            r.confirmarConTarifas(
                List.of(
                    new DetalleReserva(
                        unidad,
                        proveedor,
                        new Dinero(BigDecimal.ONE, Currency.getInstance("USD")))),
                clock));
    assertEquals(EstadoReserva.PENDIENTE, r.estado());
    assertTrue(r.confirmacion().isEmpty());
  }

  @Test
  void rechazarYCancelarRespetanEstadosTerminales() {
    var rechazada = pendiente();
    rechazada.rechazar();
    assertThrows(ReglaNegocioException.class, rechazada::cancelar);
    assertThrows(ReglaNegocioException.class, rechazada::rechazar);
    assertThrows(
        ReglaNegocioException.class, () -> rechazada.confirmarConTarifas(List.of(linea()), clock));
    var cancelada = pendiente();
    cancelada.cancelar();
    assertThrows(ReglaNegocioException.class, cancelada::cancelar);
    assertThrows(ReglaNegocioException.class, cancelada::rechazar);
    assertThrows(
        ReglaNegocioException.class, () -> cancelada.confirmarConTarifas(List.of(linea()), clock));
    var confirmada = pendiente().confirmarConTarifas(List.of(linea()), clock);
    assertThrows(ReglaNegocioException.class, confirmada::rechazar);
    assertThrows(
        ReglaNegocioException.class, () -> confirmada.confirmarConTarifas(List.of(linea()), clock));
    confirmada.cancelar();
    assertEquals(EstadoReserva.CANCELADA, confirmada.estado());
  }

  @Test
  void validaDineroYNoPermiteSumarMonedasDiferentes() {
    assertThrows(ReglaNegocioException.class, () -> dinero("-1"));
    assertThrows(
        ReglaNegocioException.class,
        () -> dinero("1").sumar(new Dinero(BigDecimal.ONE, Currency.getInstance("USD"))));
    assertEquals(dinero("0.3"), dinero("0.1").sumar(dinero("0.2")));
  }

  @Test
  void catalogoValidaNombresImagenesYEstadosOperativos() {
    assertThrows(ReglaNegocioException.class, () -> Categoria.registrar(" "));
    assertThrows(
        ReglaNegocioException.class,
        () ->
            Herramienta.registrar(
                proveedor,
                CategoriaId.generar(),
                "Equipo",
                "Descripción",
                dinero("1"),
                Set.of(URI.create("file:///tmp/foto"))));
    var u = UnidadEquipo.registrar(HerramientaId.generar(), "EQ-1");
    u.enviarAMantenimiento();
    assertFalse(u.operativa());
    u.habilitar();
    assertTrue(u.operativa());
    u.retirar();
    assertThrows(ReglaNegocioException.class, u::habilitar);
  }
}
