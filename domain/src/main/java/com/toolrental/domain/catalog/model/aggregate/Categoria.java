package com.toolrental.domain.catalog.model.aggregate;

import com.toolrental.domain.catalog.model.valueobject.CategoriaId;
import com.toolrental.domain.common.model.valueobject.Validacion;
import java.util.Objects;

public record Categoria(CategoriaId id, String nombre) {
  public Categoria {
    Objects.requireNonNull(id);
    nombre = Validacion.texto(nombre, "nombre");
  }

  public static Categoria registrar(String nombre) {
    return new Categoria(CategoriaId.generar(), nombre);
  }
}
