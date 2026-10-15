package com.toolrental.infrastructure.reservation.adapters.out.persistence.repository;

import com.toolrental.infrastructure.reservation.adapters.out.persistence.entity.ReservationJpaEntity;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationJpaRepository extends JpaRepository<ReservationJpaEntity, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select reservation from ReservationJpaEntity reservation where reservation.id = :id")
  Optional<ReservationJpaEntity> findByIdForUpdate(@Param("id") UUID id);

  @Query(
      "select (count(reservation) > 0) from ReservationJpaEntity reservation "
          + "join reservation.items item "
          + "where item.equipmentUnitId = :equipmentUnitId "
          + "and reservation.id <> :excludedReservationId "
          + "and reservation.reservationStatus = 'CONFIRMED' "
          + "and reservation.startDate <= :returnDate "
          + "and reservation.returnDate >= :startDate")
  boolean existsConfirmedOverlap(
      @Param("equipmentUnitId") UUID equipmentUnitId,
      @Param("excludedReservationId") UUID excludedReservationId,
      @Param("startDate") LocalDate startDate,
      @Param("returnDate") LocalDate returnDate);
}
