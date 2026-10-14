package com.toolrental.application.catalog.usecase;

import com.toolrental.application.catalog.command.RegistrarCategoriaCommand;
import com.toolrental.application.catalog.dto.CategoriaResponse;
import com.toolrental.domain.catalog.model.aggregate.Categoria;
import com.toolrental.domain.catalog.port.repository.CategoriaRepository;
import com.toolrental.domain.common.exception.ReglaNegocioException;
import java.time.Clock;
import java.util.Objects;

public final class RegistrarCategoriaUseCase {
  private final Clock clock;
  private final CategoriaRepository repository;

  public RegistrarCategoriaUseCase(CategoriaRepository repository, Clock clock) {
    this.repository = Objects.requireNonNull(repository);
    this.clock = Objects.requireNonNull(clock);
  }

  public CategoriaResponse execute(RegistrarCategoriaCommand command) {
    var categoria = Categoria.registrar(command.nombre(), clock);
    if (repository.listar().stream().anyMatch(c -> c.nombre().equals(categoria.nombre())))
      throw new ReglaNegocioException("Nombre de categoría duplicado");
    repository.guardar(categoria);
    return CategoriaResponse.desde(categoria);
  }
}
