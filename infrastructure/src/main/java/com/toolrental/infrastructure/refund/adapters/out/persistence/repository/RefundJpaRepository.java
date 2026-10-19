package com.toolrental.infrastructure.refund.adapters.out.persistence.repository;

import com.toolrental.infrastructure.refund.adapters.out.persistence.entity.RefundJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundJpaRepository extends JpaRepository<RefundJpaEntity, UUID> {
  Optional<RefundJpaEntity> findByKey(String key);

  List<RefundJpaEntity> findByPaymentIdOrderByRecordedAt(UUID id);

  List<RefundJpaEntity> findByTerminationIdOrderByRecordedAt(UUID id);
}
