package com.toolrental.domain.reservationextension.port.repository;

import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservationextension.model.aggregate.ReservationExtension;
import com.toolrental.domain.reservationextension.model.valueobject.ReservationExtensionId;
import java.util.List;
import java.util.Optional;

public interface ReservationExtensionRepository {
  Optional<ReservationExtension> findById(ReservationExtensionId id);

  Optional<ReservationExtension> findByIdempotencyKey(String idempotencyKey);

  Optional<ReservationExtension> findOpenByReservation(ReservationId reservationId);

  List<ReservationExtension> findByReservation(ReservationId reservationId);

  ReservationExtension save(ReservationExtension extension);
}
