package com.toolrental.application.catalog.usecase;

import com.toolrental.application.catalog.command.RegistrarUnidadCommand;
import com.toolrental.application.catalog.dto.UnidadEquipoResponse;
import com.toolrental.domain.catalog.port.repository.RegistroUnidadAtomico;
import java.util.Objects;

public final class RegistrarUnidadUseCase {
  private final RegistroUnidadAtomico registro;

  public RegistrarUnidadUseCase(RegistroUnidadAtomico registro) {
    this.registro = Objects.requireNonNull(registro);
  }

  public UnidadEquipoResponse execute(RegistrarUnidadCommand c) {
    return UnidadEquipoResponse.desde(registro.registrar(c.herramientaId(), c.codigoInventario()));
  }
}
