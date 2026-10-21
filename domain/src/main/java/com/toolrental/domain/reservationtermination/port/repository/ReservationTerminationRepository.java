package com.toolrental.domain.reservationtermination.port.repository;

import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservationtermination.model.aggregate.ReservationTermination;
import com.toolrental.domain.reservationtermination.model.valueobject.ReservationTerminationId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationTerminationRepository {
  Optional<ReservationTermination> findById(ReservationTerminationId id);

  Optional<ReservationTermination> findByReservation(ReservationId reservationId);

  Optional<ReservationTermination> findByIdempotencyKey(String key);

  List<ReservationTermination> findByParticipant(UUID participantId);

  ReservationTermination save(ReservationTermination value);
}
