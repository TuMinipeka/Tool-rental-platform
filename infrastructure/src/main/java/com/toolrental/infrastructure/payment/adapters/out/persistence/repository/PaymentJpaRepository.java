package com.toolrental.infrastructure.payment.adapters.out.persistence.repository;

import com.toolrental.infrastructure.payment.adapters.out.persistence.entity.PaymentJpaEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, UUID> {
  Optional<PaymentJpaEntity> findByIdempotencyKey(String idempotencyKey);

  List<PaymentJpaEntity> findByReservationIdOrderByRecordedAt(UUID reservationId);

  boolean existsByReservationIdAndPaymentStageAndPaymentStatus(
      UUID reservationId, String paymentStage, String paymentStatus);

  @Query(
      value =
          """
          SELECT DISTINCT payment.*
            FROM payments payment
            JOIN reservations reservation
              ON reservation.reservation_id = payment.reservation_id
            JOIN reservation_items item
              ON item.reservation_id = reservation.reservation_id
            JOIN equipment_units unit
              ON unit.equipment_unit_id = item.equipment_unit_id
            JOIN tools tool
              ON tool.tool_id = unit.tool_id
           WHERE reservation.customer_id = :participantId
              OR tool.supplier_id = :participantId
           ORDER BY payment.recorded_at DESC
          """,
      nativeQuery = true)
  List<PaymentJpaEntity> findByParticipantId(@Param("participantId") UUID participantId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select row from PaymentJpaEntity row where row.id = :id")
  Optional<PaymentJpaEntity> findLocked(@Param("id") UUID id);
}
