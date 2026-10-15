package com.toolrental.infrastructure.identity.adapters.out.persistence.repository;

import com.toolrental.infrastructure.identity.adapters.out.persistence.entity.SupplierProfileJpaEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplierProfileJpaRepository
    extends JpaRepository<SupplierProfileJpaEntity, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select supplier from SupplierProfileJpaEntity supplier where supplier.id = :id")
  Optional<SupplierProfileJpaEntity> findByIdForUpdate(@Param("id") UUID id);
}
