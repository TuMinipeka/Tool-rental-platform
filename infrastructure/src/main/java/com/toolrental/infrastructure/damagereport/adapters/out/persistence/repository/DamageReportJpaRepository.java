package com.toolrental.infrastructure.damagereport.adapters.out.persistence.repository;

import com.toolrental.infrastructure.damagereport.adapters.out.persistence.entity.DamageReportJpaEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DamageReportJpaRepository extends JpaRepository<DamageReportJpaEntity, UUID> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select row from DamageReportJpaEntity row where row.id = :id")
  Optional<DamageReportJpaEntity> findLocked(@Param("id") UUID id);
}
