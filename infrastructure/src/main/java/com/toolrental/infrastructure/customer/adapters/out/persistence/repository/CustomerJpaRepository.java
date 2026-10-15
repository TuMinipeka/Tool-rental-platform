package com.toolrental.infrastructure.customer.adapters.out.persistence.repository;

import com.toolrental.infrastructure.customer.adapters.out.persistence.entity.CustomerJpaEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, UUID> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select row from CustomerJpaEntity row where row.id = :id")
  Optional<CustomerJpaEntity> findLocked(@Param("id") UUID id);
}
