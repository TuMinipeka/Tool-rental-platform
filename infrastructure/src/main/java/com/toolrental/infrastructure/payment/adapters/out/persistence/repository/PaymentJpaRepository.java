package com.toolrental.infrastructure.payment.adapters.out.persistence.repository;

import com.toolrental.infrastructure.payment.adapters.out.persistence.entity.PaymentJpaEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, UUID> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select row from PaymentJpaEntity row where row.id = :id")
  Optional<PaymentJpaEntity> findLocked(@Param("id") UUID id);
}
