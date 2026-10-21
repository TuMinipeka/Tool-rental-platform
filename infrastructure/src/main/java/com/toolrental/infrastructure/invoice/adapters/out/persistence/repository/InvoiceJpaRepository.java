package com.toolrental.infrastructure.invoice.adapters.out.persistence.repository;

import com.toolrental.infrastructure.invoice.adapters.out.persistence.entity.InvoiceJpaEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceJpaRepository extends JpaRepository<InvoiceJpaEntity, UUID> {
  Optional<InvoiceJpaEntity> findByReservationId(UUID reservationId);

  @Query(
      value =
          """
          SELECT DISTINCT invoice.*
            FROM invoices invoice
            JOIN reservations reservation
              ON reservation.reservation_id = invoice.reservation_id
            JOIN reservation_items item
              ON item.reservation_id = reservation.reservation_id
            JOIN equipment_units unit
              ON unit.equipment_unit_id = item.equipment_unit_id
            JOIN tools tool
              ON tool.tool_id = unit.tool_id
           WHERE reservation.customer_id = :participantId
              OR tool.supplier_id = :participantId
           ORDER BY invoice.issued_at DESC
          """,
      nativeQuery = true)
  List<InvoiceJpaEntity> findByParticipantId(@Param("participantId") UUID participantId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select row from InvoiceJpaEntity row where row.id = :id")
  Optional<InvoiceJpaEntity> findLocked(@Param("id") UUID id);
}
