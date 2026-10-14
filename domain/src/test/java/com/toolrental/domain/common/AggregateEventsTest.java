package com.toolrental.domain.common;

import static org.junit.jupiter.api.Assertions.*;

import com.toolrental.domain.catalog.event.CategoriaRegistrada;
import com.toolrental.domain.catalog.event.HerramientaRegistrada;
import com.toolrental.domain.catalog.event.UnidadEquipoRegistrada;
import com.toolrental.domain.catalog.model.aggregate.Categoria;
import com.toolrental.domain.catalog.model.aggregate.Herramienta;
import com.toolrental.domain.catalog.model.aggregate.UnidadEquipo;
import com.toolrental.domain.common.model.valueobject.ClienteId;
import com.toolrental.domain.common.model.valueobject.Dinero;
import com.toolrental.domain.common.model.valueobject.ProveedorId;
import com.toolrental.domain.reservation.event.ReservaCancelada;
import com.toolrental.domain.reservation.event.ReservaConfirmada;
import com.toolrental.domain.reservation.event.ReservaRechazada;
import com.toolrental.domain.reservation.event.ReservaSolicitada;
import com.toolrental.domain.reservation.model.aggregate.Reserva;
import com.toolrental.domain.reservation.model.entity.DetalleReserva;
import com.toolrental.domain.reservation.model.valueobject.PeriodoReserva;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AggregateEventsTest {
  private final Clock clock = Clock.fixed(Instant.parse("2026-10-14T12:00:00Z"), ZoneOffset.UTC);

  @Test
  void registraEventosTipadosConElRelojInyectado() {
    var categoria = Categoria.registrar("Perforación", clock);
    var herramienta =
        Herramienta.registrar(
            ProveedorId.generar(),
            categoria.id(),
            "Taladro",
            "Industrial",
            new Dinero(BigDecimal.TEN, Currency.getInstance("COP")),
            Set.of(),
            clock);
    var unidad = UnidadEquipo.registrar(herramienta.id(), "EQ-1", clock);
    assertInstanceOf(CategoriaRegistrada.class, categoria.domainEvents().getFirst());
    assertInstanceOf(HerramientaRegistrada.class, herramienta.domainEvents().getFirst());
    assertInstanceOf(UnidadEquipoRegistrada.class, unidad.domainEvents().getFirst());
    assertEquals(clock.instant(), herramienta.domainEvents().getFirst().occurredOn());
    assertEquals(clock.instant(), unidad.domainEvents().getFirst().occurredOn());
  }

  @Test
  void eventosSonUnaInstantaneaInmutableQueSePuedeLimpiar() {
    var categoria = Categoria.registrar("Catálogo", clock);
    var eventos = categoria.domainEvents();
    assertThrows(UnsupportedOperationException.class, eventos::clear);
    categoria.clearDomainEvents();
    assertTrue(categoria.domainEvents().isEmpty());
    assertEquals(1, eventos.size());
  }

  @Test
  void restaurarCategoriaNoVuelveAGenerarEventoDeRegistro() {
    var categoria = Categoria.registrar("Catálogo", clock);
    var restaurada = Categoria.restaurar(categoria.id(), categoria.nombre());
    assertEquals(categoria.id(), restaurada.id());
    assertTrue(restaurada.domainEvents().isEmpty());
  }

  @Test
  void copiaDeReservaNoRepiteEventosYConfirmacionRegistraSoloSuTransicion() {
    var pendiente = solicitar();
    assertInstanceOf(ReservaSolicitada.class, pendiente.domainEvents().getFirst());
    assertTrue(pendiente.copia().domainEvents().isEmpty());
    var confirmada = pendiente.confirmarConTarifas(pendiente.detalles(), clock);
    assertEquals(1, confirmada.domainEvents().size());
    assertInstanceOf(ReservaConfirmada.class, confirmada.domainEvents().getFirst());
    assertEquals(confirmada.confirmacion().orElseThrow(), confirmada.domainEvents().getFirst());
    confirmada.clearDomainEvents();
    assertTrue(confirmada.confirmacion().isPresent());
  }

  @Test
  void rechazarYCancelarRegistranLaTransicionYUnFalloNoAgregaEventos() {
    var rechazada = solicitar();
    rechazada.clearDomainEvents();
    rechazada.rechazar(clock);
    assertInstanceOf(ReservaRechazada.class, rechazada.domainEvents().getFirst());
    assertThrows(
        com.toolrental.domain.common.exception.ReglaNegocioException.class,
        () -> rechazada.cancelar(clock));
    assertEquals(1, rechazada.domainEvents().size());
    var cancelada = solicitar();
    cancelada.clearDomainEvents();
    cancelada.cancelar(clock);
    assertInstanceOf(ReservaCancelada.class, cancelada.domainEvents().getFirst());
  }

  @Test
  void eventoNoAdmiteIdentidadNiFechaNulas() {
    assertThrows(NullPointerException.class, () -> new CategoriaRegistrada(null, clock.instant()));
    var categoria = Categoria.registrar("Catálogo", clock);
    assertThrows(NullPointerException.class, () -> new CategoriaRegistrada(categoria.id(), null));
  }

  @Test
  void restaurarHerramientaYUnidadConservaIdentidadEstadoYSinEventosNuevos() {
    var categoria = Categoria.registrar("Catálogo", clock);
    var herramienta =
        Herramienta.registrar(
            ProveedorId.generar(),
            categoria.id(),
            "Taladro",
            "Industrial",
            new Dinero(BigDecimal.TEN, Currency.getInstance("COP")),
            Set.of(),
            clock);
    herramienta.retirarPublicacion();
    var restaurada =
        Herramienta.restaurar(
            herramienta.id(),
            herramienta.proveedorId(),
            herramienta.categoriaId(),
            herramienta.nombre(),
            herramienta.descripcion(),
            herramienta.tarifaDiaria(),
            herramienta.imagenes(),
            herramienta.publicada());
    assertEquals(herramienta.id(), restaurada.id());
    assertFalse(restaurada.publicada());
    assertTrue(restaurada.domainEvents().isEmpty());
    var unidad = UnidadEquipo.registrar(herramienta.id(), "EQ-1", clock);
    unidad.enviarAMantenimiento();
    var restauradaUnidad =
        UnidadEquipo.restaurar(
            unidad.id(), unidad.herramientaId(), unidad.codigoInventario(), unidad.estado());
    assertEquals(unidad.id(), restauradaUnidad.id());
    assertFalse(restauradaUnidad.operativa());
    assertTrue(restauradaUnidad.domainEvents().isEmpty());
  }

  @Test
  void restaurarReservaConservaConfirmacionSinReemitirEventosYValidaEstado() {
    var pendiente = solicitar();
    var confirmada = pendiente.confirmarConTarifas(pendiente.detalles(), clock);
    var restaurada =
        Reserva.restaurar(
            confirmada.id(),
            confirmada.clienteId(),
            confirmada.periodo(),
            confirmada.detalles(),
            confirmada.creadaEn(),
            confirmada.estado(),
            confirmada.confirmacion().orElseThrow().occurredOn());
    assertEquals(confirmada.id(), restaurada.id());
    assertEquals(confirmada.estado(), restaurada.estado());
    assertEquals(confirmada.total(), restaurada.total());
    assertEquals(confirmada.confirmacion(), restaurada.confirmacion());
    assertTrue(restaurada.domainEvents().isEmpty());
    assertThrows(
        com.toolrental.domain.common.exception.ReglaNegocioException.class,
        () ->
            Reserva.restaurar(
                confirmada.id(),
                confirmada.clienteId(),
                confirmada.periodo(),
                confirmada.detalles(),
                confirmada.creadaEn(),
                confirmada.estado(),
                null));
  }

  private Reserva solicitar() {
    var categoria = Categoria.registrar("Catálogo", clock);
    var proveedor = ProveedorId.generar();
    var tarifa = new Dinero(BigDecimal.TEN, Currency.getInstance("COP"));
    var herramienta =
        Herramienta.registrar(
            proveedor, categoria.id(), "Taladro", "Industrial", tarifa, Set.of(), clock);
    var unidad = UnidadEquipo.registrar(herramienta.id(), "EQ-1", clock);
    var fecha = LocalDate.of(2026, 10, 14);
    return Reserva.solicitar(
        ClienteId.generar(),
        new PeriodoReserva(fecha, fecha),
        List.of(new DetalleReserva(unidad.id(), proveedor, tarifa)),
        clock);
  }
}
