package com.toolrental.domain.catalog.model.aggregate;

import com.toolrental.domain.catalog.model.valueobject.EstadoOperativo;
import com.toolrental.domain.catalog.model.valueobject.HerramientaId;
import com.toolrental.domain.catalog.model.valueobject.UnidadEquipoId;
import com.toolrental.domain.common.exception.ReglaNegocioException;
import com.toolrental.domain.common.model.valueobject.Validacion;
import java.util.Objects;

public final class UnidadEquipo {
  private final UnidadEquipoId id;
  private final HerramientaId herramientaId;
  private final String codigoInventario;
  private EstadoOperativo estado = EstadoOperativo.OPERATIVA;

  private UnidadEquipo(HerramientaId herramienta, String codigo) {
    id = UnidadEquipoId.generar();
    herramientaId = Objects.requireNonNull(herramienta);
    codigoInventario = Validacion.texto(codigo, "codigoInventario");
  }

  public static UnidadEquipo registrar(HerramientaId herramienta, String codigo) {
    return new UnidadEquipo(herramienta, codigo);
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
