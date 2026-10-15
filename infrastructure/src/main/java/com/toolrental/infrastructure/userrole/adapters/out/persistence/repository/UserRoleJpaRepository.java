package com.toolrental.infrastructure.userrole.adapters.out.persistence.repository;

import com.toolrental.infrastructure.userrole.adapters.out.persistence.entity.UserRoleJpaEntity;
import com.toolrental.infrastructure.userrole.adapters.out.persistence.entity.UserRoleJpaId;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRoleJpaRepository extends JpaRepository<UserRoleJpaEntity, UserRoleJpaId> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select row from UserRoleJpaEntity row where row.id = :id")
  Optional<UserRoleJpaEntity> findLocked(@Param("id") UserRoleJpaId id);
}
