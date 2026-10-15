package com.toolrental.infrastructure.invoice.adapters.out.persistence.repository;

import com.toolrental.infrastructure.invoice.adapters.out.persistence.entity.InvoiceJpaEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceJpaRepository extends JpaRepository<InvoiceJpaEntity, UUID> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select row from InvoiceJpaEntity row where row.id = :id")
  Optional<InvoiceJpaEntity> findLocked(@Param("id") UUID id);
}
