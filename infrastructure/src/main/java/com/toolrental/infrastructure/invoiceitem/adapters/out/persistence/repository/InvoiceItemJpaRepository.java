package com.toolrental.infrastructure.invoiceitem.adapters.out.persistence.repository;

import com.toolrental.infrastructure.invoiceitem.adapters.out.persistence.entity.InvoiceItemJpaEntity;
import com.toolrental.infrastructure.invoiceitem.adapters.out.persistence.entity.InvoiceItemJpaId;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceItemJpaRepository
    extends JpaRepository<InvoiceItemJpaEntity, InvoiceItemJpaId> {

  List<InvoiceItemJpaEntity> findByIdInvoiceIdOrderByIdLineNumber(UUID invoiceId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select row from InvoiceItemJpaEntity row where row.id = :id")
  Optional<InvoiceItemJpaEntity> findLocked(@Param("id") InvoiceItemJpaId id);
}
