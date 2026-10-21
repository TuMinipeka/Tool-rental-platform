package com.toolrental.infrastructure.userrole.adapters.out.persistence.repository;

import com.toolrental.infrastructure.userrole.adapters.out.persistence.entity.UserRoleJpaEntity;
import com.toolrental.infrastructure.userrole.adapters.out.persistence.entity.UserRoleJpaId;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRoleJpaRepository extends JpaRepository<UserRoleJpaEntity, UserRoleJpaId> {

  @Query(
      value =
          """
          SELECT role.code
            FROM user_roles assignment
            JOIN roles role ON role.role_id = assignment.role_id
           WHERE assignment.user_id = :userId
          """,
      nativeQuery = true)
  Set<String> findRoleCodesByUserId(@Param("userId") UUID userId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select row from UserRoleJpaEntity row where row.id = :id")
  Optional<UserRoleJpaEntity> findLocked(@Param("id") UserRoleJpaId id);
}
