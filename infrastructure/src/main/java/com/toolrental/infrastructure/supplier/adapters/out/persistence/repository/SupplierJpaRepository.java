package com.toolrental.infrastructure.supplier.adapters.out.persistence.repository;

import com.toolrental.infrastructure.supplier.adapters.out.persistence.entity.SupplierJpaEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplierJpaRepository extends JpaRepository<SupplierJpaEntity, UUID> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select supplier from SupplierJpaEntity supplier where supplier.id = :id")
  Optional<SupplierJpaEntity> findByIdForUpdate(@Param("id") UUID id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select row from SupplierJpaEntity row where row.id = :id")
  Optional<SupplierJpaEntity> findLocked(@Param("id") UUID id);
}
