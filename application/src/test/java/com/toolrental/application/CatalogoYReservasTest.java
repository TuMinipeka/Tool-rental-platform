package com.toolrental.application;

import static org.junit.jupiter.api.Assertions.*;

import com.toolrental.application.catalog.command.RegistrarCategoriaCommand;
import com.toolrental.application.catalog.command.RegistrarHerramientaCommand;
import com.toolrental.application.catalog.command.RegistrarUnidadCommand;
import com.toolrental.application.catalog.usecase.ConsultarCatalogoUseCase;
import com.toolrental.application.catalog.usecase.RegistrarCategoriaUseCase;
import com.toolrental.application.catalog.usecase.RegistrarHerramientaUseCase;
import com.toolrental.application.catalog.usecase.RegistrarUnidadUseCase;
import com.toolrental.application.common.exception.RecursoNoEncontradoException;
import com.toolrental.application.reservation.command.SolicitarReservaCommand;
import com.toolrental.application.reservation.dto.ReservaResponse;
import com.toolrental.application.reservation.usecase.CancelarReservaUseCase;
import com.toolrental.application.reservation.usecase.ConfirmarReservaUseCase;
import com.toolrental.application.reservation.usecase.RechazarReservaUseCase;
import com.toolrental.application.reservation.usecase.SolicitarReservaUseCase;
import com.toolrental.domain.catalog.model.valueobject.CategoriaId;
import com.toolrental.domain.catalog.model.valueobject.HerramientaId;
import com.toolrental.domain.catalog.model.valueobject.UnidadEquipoId;
import com.toolrental.domain.common.exception.ReglaNegocioException;
import com.toolrental.domain.common.model.valueobject.ClienteId;
import com.toolrental.domain.common.model.valueobject.Dinero;
import com.toolrental.domain.common.model.valueobject.ProveedorId;
import com.toolrental.domain.reservation.model.valueobject.EstadoReserva;
import com.toolrental.domain.reservation.model.valueobject.PeriodoReserva;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CatalogoYReservasTest {
  private final Fixture f = new Fixture();
  private final Clock clock =
      Clock.fixed(Instant.parse("2026-10-14T12:00:00Z"), ZoneId.of("America/Bogota"));
  private final ClienteId cliente = ClienteId.generar();
  private final ProveedorId proveedor = ProveedorId.generar();
  private final LocalDate fecha = LocalDate.of(2026, 10, 14);
  private final RegistrarCategoriaUseCase registrarCategoria =
      new RegistrarCategoriaUseCase(f.categoriaRepository);
  private final RegistrarHerramientaUseCase registrarHerramienta =
      new RegistrarHerramientaUseCase(
          f.herramientaRepository, f.categoriaRepository, f.participantes);
  private final RegistrarUnidadUseCase registrarUnidad =
      new RegistrarUnidadUseCase(f.registroUnidad);
  private final SolicitarReservaUseCase solicitar =
      new SolicitarReservaUseCase(
          f.reservaRepository, f.unidadRepository, f.herramientaRepository, f.participantes, clock);
  private final ConfirmarReservaUseCase confirmar = new ConfirmarReservaUseCase(f.gestion, clock);
  private final CancelarReservaUseCase cancelar = new CancelarReservaUseCase(f.gestion);
  private final RechazarReservaUseCase rechazar = new RechazarReservaUseCase(f.gestion);
  private final CategoriaId categoria;
  private final HerramientaId herramienta;
  private final UnidadEquipoId unidad;

  CatalogoYReservasTest() {
    f.clientes.add(cliente);
    f.proveedores.add(proveedor);
    categoria = registrarCategoria.execute(new RegistrarCategoriaCommand("Perforación")).id();
    herramienta = crearHerramienta(proveedor);
    unidad = registrarUnidad.execute(new RegistrarUnidadCommand(herramienta, "T-01")).id();
  }

  private Dinero dinero(String valor) {
    return new Dinero(new BigDecimal(valor), Currency.getInstance("COP"));
  }

  private HerramientaId crearHerramienta(ProveedorId p) {
    return registrarHerramienta
        .execute(
            new RegistrarHerramientaCommand(
                p, categoria, "Taladro", "Taladro industrial", dinero("10000"), Set.of()))
        .id();
  }

  private ReservaResponse solicitar(UnidadEquipoId... ids) {
    return solicitar.execute(
        new SolicitarReservaCommand(
            cliente, new PeriodoReserva(fecha, fecha.plusDays(1)), List.of(ids)));
  }

  @Test
  void registraYConsultaCatalogoSinExponerAgregadosMutables() {
    var consultar =
        new ConsultarCatalogoUseCase(
            f.categoriaRepository, f.herramientaRepository, f.unidadRepository);
    assertEquals("Perforación", consultar.categorias().getFirst().nombre());
    assertEquals(herramienta, consultar.herramientas().getFirst().id());
    assertEquals(unidad, consultar.unidades(herramienta).getFirst().id());
    var snapshot = consultar.herramienta(herramienta);
    f.herramientas.get(herramienta).cambiarTarifa(dinero("50"));
    assertEquals(dinero("10000"), snapshot.tarifaDiaria());
  }

  @Test
  void validaReferenciasYUnicidadDeCodigoPorProveedor() {
    assertThrows(
        ReglaNegocioException.class,
        () -> registrarCategoria.execute(new RegistrarCategoriaCommand(" Perforación ")));
    assertThrows(
        RecursoNoEncontradoException.class,
        () ->
            registrarHerramienta.execute(
                new RegistrarHerramientaCommand(
                    proveedor, CategoriaId.generar(), "X", "Y", dinero("1"), Set.of())));
    assertThrows(RecursoNoEncontradoException.class, () -> crearHerramienta(ProveedorId.generar()));
    var otraHerramienta = crearHerramienta(proveedor);
    assertThrows(
        ReglaNegocioException.class,
        () -> registrarUnidad.execute(new RegistrarUnidadCommand(otraHerramienta, "T-01")));
    var otroProveedor = ProveedorId.generar();
    f.proveedores.add(otroProveedor);
    var otroCatalogo = crearHerramienta(otroProveedor);
    assertDoesNotThrow(
        () -> registrarUnidad.execute(new RegistrarUnidadCommand(otroCatalogo, "T-01")));
  }

  @Test
  void solicitudesPendientesNoBloqueanYConflictoNoMutaReserva() {
    var primera = solicitar(unidad);
    var segunda = solicitar(unidad);
    confirmar.execute(primera.id(), proveedor);
    assertThrows(ReglaNegocioException.class, () -> confirmar.execute(segunda.id(), proveedor));
    var persistida = f.reservas.get(segunda.id());
    assertEquals(EstadoReserva.PENDIENTE, persistida.estado());
    assertTrue(persistida.confirmacion().isEmpty());
  }

  @Test
  void confirmaConTarifaVigenteYLaCongela() {
    var r = solicitar(unidad);
    f.herramientas.get(herramienta).cambiarTarifa(dinero("15000"));
    var confirmada = confirmar.execute(r.id(), proveedor);
    assertEquals(dinero("30000"), confirmada.total());
    f.herramientas.get(herramienta).cambiarTarifa(dinero("20000"));
    assertEquals(dinero("30000"), f.reservas.get(r.id()).total());
    assertThrows(ReglaNegocioException.class, () -> confirmar.execute(r.id(), proveedor));
  }

  @Test
  void reservaQueComparteFechaFinalSeBloqueaYLiberacionPermiteConfirmar() {
    var primera = solicitar(unidad);
    confirmar.execute(primera.id(), proveedor);
    var borde =
        solicitar.execute(
            new SolicitarReservaCommand(
                cliente,
                new PeriodoReserva(fecha.plusDays(1), fecha.plusDays(2)),
                List.of(unidad)));
    assertThrows(ReglaNegocioException.class, () -> confirmar.execute(borde.id(), proveedor));
    cancelar.execute(primera.id(), cliente);
    assertEquals(EstadoReserva.CONFIRMADA, confirmar.execute(borde.id(), proveedor).estado());
  }

  @Test
  void reservaPosteriorSinFechaCompartidaSePuedeConfirmar() {
    confirmar.execute(solicitar(unidad).id(), proveedor);
    var posterior =
        solicitar.execute(
            new SolicitarReservaCommand(
                cliente,
                new PeriodoReserva(fecha.plusDays(2), fecha.plusDays(2)),
                List.of(unidad)));
    assertEquals(EstadoReserva.CONFIRMADA, confirmar.execute(posterior.id(), proveedor).estado());
  }

  @Test
  void fallaAtomicaEnSegundaUnidadNoConfirmaParcialmente() {
    var segunda = registrarUnidad.execute(new RegistrarUnidadCommand(herramienta, "T-02")).id();
    confirmar.execute(solicitar(segunda).id(), proveedor);
    var varias = solicitar(unidad, segunda);
    assertThrows(ReglaNegocioException.class, () -> confirmar.execute(varias.id(), proveedor));
    assertEquals(EstadoReserva.PENDIENTE, f.reservas.get(varias.id()).estado());
    assertTrue(f.reservas.get(varias.id()).confirmacion().isEmpty());
    assertDoesNotThrow(() -> confirmar.execute(solicitar(unidad).id(), proveedor));
  }

  @Test
  void revalidaEstadoOperativoMonedaYPerfilesAlConfirmar() {
    var r = solicitar(unidad);
    f.unidades.get(unidad).enviarAMantenimiento();
    assertThrows(ReglaNegocioException.class, () -> confirmar.execute(r.id(), proveedor));
    f.unidades.get(unidad).habilitar();
    f.herramientas
        .get(herramienta)
        .cambiarTarifa(new Dinero(BigDecimal.ONE, Currency.getInstance("USD")));
    assertThrows(ReglaNegocioException.class, () -> confirmar.execute(r.id(), proveedor));
    f.herramientas.get(herramienta).cambiarTarifa(dinero("1"));
    f.clientes.remove(cliente);
    assertThrows(ReglaNegocioException.class, () -> confirmar.execute(r.id(), proveedor));
    assertEquals(EstadoReserva.PENDIENTE, f.reservas.get(r.id()).estado());
  }

  @Test
  void rechazarYCancelarValidanActorYEstado() {
    var r = solicitar(unidad);
    assertThrows(
        ReglaNegocioException.class, () -> confirmar.execute(r.id(), ProveedorId.generar()));
    assertThrows(ReglaNegocioException.class, () -> cancelar.execute(r.id(), ClienteId.generar()));
    assertEquals(EstadoReserva.RECHAZADA, rechazar.execute(r.id(), proveedor).estado());
    assertThrows(ReglaNegocioException.class, () -> cancelar.execute(r.id(), cliente));
    assertThrows(ReglaNegocioException.class, () -> confirmar.execute(r.id(), proveedor));
    var otra = solicitar(unidad);
    cancelar.execute(otra.id(), cliente);
    assertThrows(ReglaNegocioException.class, () -> rechazar.execute(otra.id(), proveedor));
  }

  @Test
  void solicitudNoGuardaCuandoFallaValidacion() {
    int antes = f.reservas.size();
    assertThrows(ReglaNegocioException.class, () -> solicitar(unidad, unidad));
    assertThrows(RecursoNoEncontradoException.class, () -> solicitar(UnidadEquipoId.generar()));
    var p = ProveedorId.generar();
    f.proveedores.add(p);
    var u = registrarUnidad.execute(new RegistrarUnidadCommand(crearHerramienta(p), "Otro")).id();
    assertThrows(ReglaNegocioException.class, () -> solicitar(unidad, u));
    f.clientes.remove(cliente);
    assertThrows(RecursoNoEncontradoException.class, () -> solicitar(unidad));
    assertEquals(antes, f.reservas.size());
  }

  @Test
  void puertoAtomicoDescartaCambiosSiLaOperacionFalla() {
    var r = solicitar(unidad);
    assertThrows(
        IllegalStateException.class,
        () ->
            f.gestion.ejecutar(
                r.id(),
                contexto -> {
                  contexto.reserva().cancelar();
                  throw new IllegalStateException("Simulación de fallo antes de guardar");
                }));
    assertEquals(EstadoReserva.PENDIENTE, f.reservas.get(r.id()).estado());
  }
}
