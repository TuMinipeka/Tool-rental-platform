package com.toolrental.application.catalog.dto;

import com.toolrental.domain.catalog.model.aggregate.UnidadEquipo;
import com.toolrental.domain.catalog.model.valueobject.EstadoOperativo;
import com.toolrental.domain.catalog.model.valueobject.HerramientaId;
import com.toolrental.domain.catalog.model.valueobject.UnidadEquipoId;

public record UnidadEquipoResponse(
    UnidadEquipoId id,
    HerramientaId herramientaId,
    String codigoInventario,
    EstadoOperativo estado) {
  public static UnidadEquipoResponse desde(UnidadEquipo c) {
    return new UnidadEquipoResponse(c.id(), c.herramientaId(), c.codigoInventario(), c.estado());
  }
}
