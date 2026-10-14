package com.toolrental.domain.catalog.model.aggregate;

import com.toolrental.domain.catalog.event.UnidadEquipoRegistrada;
import com.toolrental.domain.catalog.model.valueobject.EstadoOperativo;
import com.toolrental.domain.catalog.model.valueobject.HerramientaId;
import com.toolrental.domain.catalog.model.valueobject.UnidadEquipoId;
import com.toolrental.domain.common.exception.ReglaNegocioException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.common.model.valueobject.Validacion;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class UnidadEquipo extends AggregateRoot {
  private final UnidadEquipoId id;
  private final HerramientaId herramientaId;
  private final String codigoInventario;
  private EstadoOperativo estado;

  private UnidadEquipo(
      UnidadEquipoId id, HerramientaId herramienta, String codigo, EstadoOperativo estado) {
    this.id = Objects.requireNonNull(id);
    this.estado = Objects.requireNonNull(estado);
    herramientaId = Objects.requireNonNull(herramienta);
    codigoInventario = Validacion.texto(codigo, "codigoInventario");
  }

  public static UnidadEquipo registrar(HerramientaId herramienta, String codigo, Clock clock) {
    var unidad =
        new UnidadEquipo(UnidadEquipoId.generar(), herramienta, codigo, EstadoOperativo.OPERATIVA);
    unidad.recordEvent(new UnidadEquipoRegistrada(unidad.id, Instant.now(clock)));
    return unidad;
  }

  public static UnidadEquipo restaurar(
      UnidadEquipoId id, HerramientaId herramienta, String codigo, EstadoOperativo estado) {
    return new UnidadEquipo(id, herramienta, codigo, estado);
  }

  public void enviarAMantenimiento() {
    exigirNoRetirada();
    estado = EstadoOperativo.MANTENIMIENTO;
  }

  public void habilitar() {
    exigirNoRetirada();
    estado = EstadoOperativo.OPERATIVA;
  }

  public void retirar() {
    exigirNoRetirada();
    estado = EstadoOperativo.RETIRADA;
  }

  private void exigirNoRetirada() {
    if (estado == EstadoOperativo.RETIRADA) throw new ReglaNegocioException("Unidad retirada");
  }

  public UnidadEquipoId id() {
    return id;
  }

  public HerramientaId herramientaId() {
    return herramientaId;
  }

  public String codigoInventario() {
    return codigoInventario;
  }

  public EstadoOperativo estado() {
    return estado;
  }

  public boolean operativa() {
    return estado == EstadoOperativo.OPERATIVA;
  }
}
