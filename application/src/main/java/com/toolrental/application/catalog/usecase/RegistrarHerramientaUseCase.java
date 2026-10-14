package com.toolrental.application.catalog.usecase;

import com.toolrental.application.catalog.command.RegistrarHerramientaCommand;
import com.toolrental.application.catalog.dto.HerramientaResponse;
import com.toolrental.application.common.exception.RecursoNoEncontradoException;
import com.toolrental.domain.catalog.model.aggregate.Herramienta;
import com.toolrental.domain.catalog.port.repository.CategoriaRepository;
import com.toolrental.domain.catalog.port.repository.HerramientaRepository;
import com.toolrental.domain.common.port.ParticipantesPort;
import java.util.Objects;

public final class RegistrarHerramientaUseCase {
  private final HerramientaRepository herramientas;
  private final CategoriaRepository categorias;
  private final ParticipantesPort participantes;

  public RegistrarHerramientaUseCase(
      HerramientaRepository h, CategoriaRepository c, ParticipantesPort p) {
    herramientas = Objects.requireNonNull(h);
    categorias = Objects.requireNonNull(c);
    participantes = Objects.requireNonNull(p);
  }

  public HerramientaResponse execute(RegistrarHerramientaCommand c) {
    categorias
        .buscar(c.categoriaId())
        .orElseThrow(() -> new RecursoNoEncontradoException("Categoría"));
    if (!participantes.proveedorActivo(c.proveedorId()))
      throw new RecursoNoEncontradoException("Proveedor activo");
    var h =
        Herramienta.registrar(
            c.proveedorId(),
            c.categoriaId(),
            c.nombre(),
            c.descripcion(),
            c.tarifaDiaria(),
            c.imagenes());
    herramientas.guardar(h);
    return HerramientaResponse.desde(h);
  }
}
