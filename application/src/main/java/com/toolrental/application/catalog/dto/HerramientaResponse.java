package com.toolrental.application.catalog.dto;

import com.toolrental.domain.catalog.model.aggregate.Herramienta;
import com.toolrental.domain.catalog.model.valueobject.CategoriaId;
import com.toolrental.domain.catalog.model.valueobject.HerramientaId;
import com.toolrental.domain.common.model.valueobject.Dinero;
import com.toolrental.domain.common.model.valueobject.ProveedorId;
import java.net.URI;
import java.util.Set;

public record HerramientaResponse(
    HerramientaId id,
    ProveedorId proveedorId,
    CategoriaId categoriaId,
    String nombre,
    String descripcion,
    Dinero tarifaDiaria,
    Set<URI> imagenes,
    boolean publicada) {
  public HerramientaResponse {
    imagenes = Set.copyOf(imagenes);
  }

  public static HerramientaResponse desde(Herramienta c) {
    return new HerramientaResponse(
        c.id(),
        c.proveedorId(),
        c.categoriaId(),
        c.nombre(),
        c.descripcion(),
        c.tarifaDiaria(),
        c.imagenes(),
        c.publicada());
  }
}
