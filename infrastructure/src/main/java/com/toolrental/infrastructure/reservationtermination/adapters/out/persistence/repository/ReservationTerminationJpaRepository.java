package com.toolrental.infrastructure.reservationtermination.adapters.out.persistence.repository;

import com.toolrental.infrastructure.reservationtermination.adapters.out.persistence.entity.ReservationTerminationJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationTerminationJpaRepository
    extends JpaRepository<ReservationTerminationJpaEntity, UUID> {
  Optional<ReservationTerminationJpaEntity> findByReservationId(UUID reservationId);

  Optional<ReservationTerminationJpaEntity> findByIdempotencyKey(String key);

  @Query(
      value =
          "SELECT t.* FROM reservation_terminations t JOIN reservations r ON r.reservation_id=t.reservation_id JOIN reservation_items i ON i.reservation_id=r.reservation_id JOIN equipment_units u ON u.equipment_unit_id=i.equipment_unit_id JOIN tools h ON h.tool_id=u.tool_id WHERE r.customer_id=:participant OR h.supplier_id=:participant ORDER BY t.requested_at DESC",
      nativeQuery = true)
  List<ReservationTerminationJpaEntity> findByParticipant(@Param("participant") UUID participant);
}
