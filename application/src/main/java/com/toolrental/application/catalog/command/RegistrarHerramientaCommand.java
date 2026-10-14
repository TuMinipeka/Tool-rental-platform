package com.toolrental.application.catalog.command;

import com.toolrental.domain.catalog.model.valueobject.CategoriaId;
import com.toolrental.domain.common.model.valueobject.Dinero;
import com.toolrental.domain.common.model.valueobject.ProveedorId;
import java.net.URI;
import java.util.Set;

public record RegistrarHerramientaCommand(
    ProveedorId proveedorId,
    CategoriaId categoriaId,
    String nombre,
    String descripcion,
    Dinero tarifaDiaria,
    Set<URI> imagenes) {
  public RegistrarHerramientaCommand {
    imagenes = Set.copyOf(imagenes);
  }
}
