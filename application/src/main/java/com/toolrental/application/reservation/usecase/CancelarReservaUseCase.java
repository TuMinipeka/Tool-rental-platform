package com.toolrental.application.reservation.usecase;

import com.toolrental.application.reservation.dto.ReservaResponse;
import com.toolrental.domain.common.exception.ReglaNegocioException;
import com.toolrental.domain.common.model.valueobject.ClienteId;
import com.toolrental.domain.reservation.model.valueobject.ReservaId;
import com.toolrental.domain.reservation.port.repository.GestionReservaAtomica;
import java.util.Objects;

public final class CancelarReservaUseCase {
  private final GestionReservaAtomica gestion;

  public CancelarReservaUseCase(GestionReservaAtomica gestion) {
    this.gestion = Objects.requireNonNull(gestion);
  }

  public ReservaResponse execute(ReservaId id, ClienteId actor) {
    Objects.requireNonNull(actor);
    return ReservaResponse.desde(
        gestion.ejecutar(
            Objects.requireNonNull(id),
            contexto -> {
              var r = contexto.reserva();
              if (!r.clienteId().equals(actor) || !contexto.clienteActivo(actor))
                throw new ReglaNegocioException("Cliente no autorizado");
              r.cancelar();
              return r;
            }));
  }
}
