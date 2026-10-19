package com.toolrental.infrastructure.reservation.adapters.out.persistence.repository;

import com.toolrental.infrastructure.reservation.adapters.out.persistence.entity.ReservationJpaEntity;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationJpaRepository extends JpaRepository<ReservationJpaEntity, UUID> {
  List<ReservationJpaEntity> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

  @Query(
      value =
          """
          SELECT DISTINCT reservation.*
            FROM reservations reservation
            JOIN reservation_items item
              ON item.reservation_id = reservation.reservation_id
            JOIN equipment_units unit
              ON unit.equipment_unit_id = item.equipment_unit_id
            JOIN tools tool
              ON tool.tool_id = unit.tool_id
           WHERE tool.supplier_id = :supplierId
           ORDER BY reservation.created_at DESC
          """,
      nativeQuery = true)
  List<ReservationJpaEntity> findBySupplierId(@Param("supplierId") UUID supplierId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select reservation from ReservationJpaEntity reservation where reservation.id = :id")
  Optional<ReservationJpaEntity> findByIdForUpdate(@Param("id") UUID id);

  @Query(
      "select (count(reservation) > 0) from ReservationJpaEntity reservation "
          + "join ReservationItemJpaEntity item on item.id.reservationId = reservation.id "
          + "where item.id.equipmentUnitId = :equipmentUnitId "
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
