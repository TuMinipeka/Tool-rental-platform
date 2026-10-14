package com.toolrental.application.reservation.usecase;

import com.toolrental.application.reservation.dto.ReservaResponse;
import com.toolrental.domain.common.exception.ReglaNegocioException;
import com.toolrental.domain.common.model.valueobject.ProveedorId;
import com.toolrental.domain.reservation.model.valueobject.ReservaId;
import com.toolrental.domain.reservation.port.repository.GestionReservaAtomica;
import java.util.Objects;

public final class RechazarReservaUseCase {
  private final GestionReservaAtomica gestion;

  public RechazarReservaUseCase(GestionReservaAtomica gestion) {
    this.gestion = Objects.requireNonNull(gestion);
  }

  public ReservaResponse execute(ReservaId id, ProveedorId actor) {
    Objects.requireNonNull(actor);
    return ReservaResponse.desde(
        gestion.ejecutar(
            Objects.requireNonNull(id),
            contexto -> {
              var r = contexto.reserva();
              if (!r.proveedorId().equals(actor) || !contexto.proveedorActivo(actor))
                throw new ReglaNegocioException("Proveedor no autorizado");
              r.rechazar();
              return r;
            }));
  }
}
