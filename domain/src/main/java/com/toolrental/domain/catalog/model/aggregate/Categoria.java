package com.toolrental.domain.catalog.model.aggregate;

import com.toolrental.domain.catalog.event.CategoriaRegistrada;
import com.toolrental.domain.catalog.model.valueobject.CategoriaId;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.common.model.valueobject.Validacion;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class Categoria extends AggregateRoot {
  private final CategoriaId id;
  private final String nombre;

  private Categoria(CategoriaId id, String nombre) {
    this.id = Objects.requireNonNull(id);
    this.nombre = Validacion.texto(nombre, "nombre");
  }

  public static Categoria registrar(String nombre, Clock clock) {
    var categoria = new Categoria(CategoriaId.generar(), nombre);
    categoria.recordEvent(new CategoriaRegistrada(categoria.id, Instant.now(clock)));
    return categoria;
  }

  public static Categoria restaurar(CategoriaId id, String nombre) {
    return new Categoria(id, nombre);
  }

  public CategoriaId id() {
    return id;
  }

  public String nombre() {
    return nombre;
  }
}
