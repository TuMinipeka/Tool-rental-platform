package com.toolrental.infrastructure.deliveryrate.adapters.out.persistence.repository;

import com.toolrental.infrastructure.deliveryrate.adapters.out.persistence.entity.DeliveryRateJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface DeliveryRateJpaRepository extends JpaRepository<DeliveryRateJpaEntity, UUID> {
  Optional<DeliveryRateJpaEntity> findFirstByActiveTrueOrderByVersionDesc();

  @Modifying
  @Query("update DeliveryRateJpaEntity row set row.active=false where row.active=true")
  void deactivateAll();
}
