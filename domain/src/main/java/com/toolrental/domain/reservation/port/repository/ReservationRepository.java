package com.toolrental.domain.reservation.port.repository;

import com.toolrental.domain.reservation.model.aggregate.Reservation;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import java.util.Optional;

/** Solo crea requestes. Las transiciones deben pasar por AtomicReservationManagement. */
public interface ReservationRepository {
  void addRequest(Reservation reservation);

  Optional<Reservation> findById(ReservationId id);
}
