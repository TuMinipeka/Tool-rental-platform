package com.toolrental.infrastructure.cancellationpolicy.adapters.out.persistence.repository;

import com.toolrental.infrastructure.cancellationpolicy.adapters.out.persistence.entity.CancellationPolicyJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CancellationPolicyJpaRepository
    extends JpaRepository<CancellationPolicyJpaEntity, UUID> {
  Optional<CancellationPolicyJpaEntity> findFirstByActiveTrueOrderByVersionDesc();

  @Modifying
  @Query("update CancellationPolicyJpaEntity row set row.active=false where row.active=true")
  void deactivateAll();
}
