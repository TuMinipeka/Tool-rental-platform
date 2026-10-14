package com.toolrental.application.catalog.dto;

import com.toolrental.domain.catalog.model.aggregate.Categoria;
import com.toolrental.domain.catalog.model.valueobject.CategoriaId;

public record CategoriaResponse(CategoriaId id, String nombre) {
  public static CategoriaResponse desde(Categoria c) {
    return new CategoriaResponse(c.id(), c.nombre());
  }
}
