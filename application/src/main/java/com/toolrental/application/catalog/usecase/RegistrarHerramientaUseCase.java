package com.toolrental.application.catalog.usecase;

import com.toolrental.application.catalog.command.RegistrarHerramientaCommand;
import com.toolrental.application.catalog.dto.HerramientaResponse;
import com.toolrental.application.catalog.exception.CategoriaNotFoundApplicationException;
import com.toolrental.application.catalog.exception.ProveedorNotFoundApplicationException;
import com.toolrental.domain.catalog.model.aggregate.Herramienta;
import com.toolrental.domain.catalog.port.repository.CategoriaRepository;
import com.toolrental.domain.catalog.port.repository.HerramientaRepository;
import com.toolrental.domain.common.port.ParticipantesPort;
import java.time.Clock;
import java.util.Objects;

public final class RegistrarHerramientaUseCase {
  private final Clock clock;
  private final HerramientaRepository herramientas;
  private final CategoriaRepository categorias;
  private final ParticipantesPort participantes;

  public RegistrarHerramientaUseCase(
      HerramientaRepository h, CategoriaRepository c, ParticipantesPort p, Clock clock) {
    herramientas = Objects.requireNonNull(h);
    categorias = Objects.requireNonNull(c);
    participantes = Objects.requireNonNull(p);
    this.clock = Objects.requireNonNull(clock);
  }

  public HerramientaResponse execute(RegistrarHerramientaCommand c) {
    categorias
        .buscar(c.categoriaId())
        .orElseThrow(() -> new CategoriaNotFoundApplicationException(c.categoriaId()));
    if (!participantes.proveedorActivo(c.proveedorId()))
      throw new ProveedorNotFoundApplicationException(c.proveedorId());
    var h =
        Herramienta.registrar(
            c.proveedorId(),
            c.categoriaId(),
            c.nombre(),
            c.descripcion(),
            c.tarifaDiaria(),
            c.imagenes(),
            clock);
    herramientas.guardar(h);
    return HerramientaResponse.desde(h);
  }
}
