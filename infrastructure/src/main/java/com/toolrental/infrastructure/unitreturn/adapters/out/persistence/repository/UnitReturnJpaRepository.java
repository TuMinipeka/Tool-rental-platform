package com.toolrental.infrastructure.unitreturn.adapters.out.persistence.repository;

import com.toolrental.infrastructure.unitreturn.adapters.out.persistence.entity.UnitReturnJpaEntity;
import com.toolrental.infrastructure.unitreturn.adapters.out.persistence.entity.UnitReturnJpaId;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UnitReturnJpaRepository
    extends JpaRepository<UnitReturnJpaEntity, UnitReturnJpaId> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select row from UnitReturnJpaEntity row where row.id = :id")
  Optional<UnitReturnJpaEntity> findLocked(@Param("id") UnitReturnJpaId id);
}
