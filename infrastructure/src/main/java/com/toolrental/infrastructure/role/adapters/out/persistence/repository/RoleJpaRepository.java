package com.toolrental.infrastructure.role.adapters.out.persistence.repository;

import com.toolrental.infrastructure.role.adapters.out.persistence.entity.RoleJpaEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, Short> {

  Optional<RoleJpaEntity> findByCode(String code);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select row from RoleJpaEntity row where row.id = :id")
  Optional<RoleJpaEntity> findLocked(@Param("id") Short id);
}
