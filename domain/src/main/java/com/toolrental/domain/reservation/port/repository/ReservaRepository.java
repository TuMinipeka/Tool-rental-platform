package com.toolrental.domain.reservation.port.repository;

import com.toolrental.domain.reservation.model.aggregate.Reserva;
import com.toolrental.domain.reservation.model.valueobject.ReservaId;
import java.util.Optional;

/** Solo crea solicitudes. Las transiciones deben pasar por GestionReservaAtomica. */
public interface ReservaRepository {
  void agregarSolicitud(Reserva reserva);

  Optional<Reserva> buscar(ReservaId id);
}
