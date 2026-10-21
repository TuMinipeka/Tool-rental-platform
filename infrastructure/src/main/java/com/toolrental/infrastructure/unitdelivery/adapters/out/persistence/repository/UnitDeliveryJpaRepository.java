package com.toolrental.infrastructure.unitdelivery.adapters.out.persistence.repository;

import com.toolrental.infrastructure.unitdelivery.adapters.out.persistence.entity.UnitDeliveryJpaEntity;
import com.toolrental.infrastructure.unitdelivery.adapters.out.persistence.entity.UnitDeliveryJpaId;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UnitDeliveryJpaRepository
    extends JpaRepository<UnitDeliveryJpaEntity, UnitDeliveryJpaId> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select row from UnitDeliveryJpaEntity row where row.id = :id")
  Optional<UnitDeliveryJpaEntity> findLocked(@Param("id") UnitDeliveryJpaId id);
}
