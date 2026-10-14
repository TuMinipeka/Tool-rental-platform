package com.toolrental.application.catalog.usecase;

import com.toolrental.application.catalog.command.RegistrarUnidadCommand;
import com.toolrental.application.catalog.dto.UnidadEquipoResponse;
import com.toolrental.domain.catalog.port.repository.RegistroUnidadAtomico;
import java.time.Clock;
import java.util.Objects;

public final class RegistrarUnidadUseCase {
  private final RegistroUnidadAtomico registro;
  private final Clock clock;

  public RegistrarUnidadUseCase(RegistroUnidadAtomico registro, Clock clock) {
    this.registro = Objects.requireNonNull(registro);
    this.clock = Objects.requireNonNull(clock);
  }

  public UnidadEquipoResponse execute(RegistrarUnidadCommand c) {
    return UnidadEquipoResponse.desde(
        registro.registrar(c.herramientaId(), c.codigoInventario(), clock));
  }
}
