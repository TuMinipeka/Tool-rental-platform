package com.toolrental.infrastructure.user.adapters.out.persistence.repository;

import com.toolrental.infrastructure.user.adapters.out.persistence.entity.UserJpaEntity;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "select account from UserJpaEntity account where account.id in :ids order by account.id")
  List<UserJpaEntity> findAllByIdForUpdate(@Param("ids") Collection<UUID> ids);

  @Query(
      value =
          """
          SELECT EXISTS (
              SELECT 1
                FROM users account
                JOIN user_roles assignment ON assignment.user_id = account.user_id
                JOIN roles role ON role.role_id = assignment.role_id
               WHERE account.user_id = :userId
                 AND account.account_status = 'ACTIVE'
                 AND role.code = :roleCode
          )
          """,
      nativeQuery = true)
  boolean hasActiveRole(@Param("userId") UUID userId, @Param("roleCode") String roleCode);
}
