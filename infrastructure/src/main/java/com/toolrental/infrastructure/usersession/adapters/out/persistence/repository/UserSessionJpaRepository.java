package com.toolrental.infrastructure.usersession.adapters.out.persistence.repository;

import com.toolrental.infrastructure.usersession.adapters.out.persistence.entity.UserSessionJpaEntity;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserSessionJpaRepository extends JpaRepository<UserSessionJpaEntity, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select session from UserSessionJpaEntity session where session.refreshTokenHash = :hash")
  Optional<UserSessionJpaEntity> findByRefreshTokenHashForUpdate(@Param("hash") String hash);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select session from UserSessionJpaEntity session where session.id = :id")
  Optional<UserSessionJpaEntity> findByIdForUpdate(@Param("id") UUID id);

  List<UserSessionJpaEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

  @Modifying
  @Query(
      "update UserSessionJpaEntity session set session.status = :status, session.revokedAt = :at "
          + "where session.familyId = :familyId and session.status in ('ACTIVE', 'ROTATED')")
  int revokeFamily(
      @Param("familyId") UUID familyId, @Param("at") Instant at, @Param("status") String status);

  @Modifying
  @Query(
      "update UserSessionJpaEntity session set session.status = 'REVOKED', session.revokedAt = :at "
          + "where session.userId = :userId and session.status in ('ACTIVE', 'ROTATED')")
  int revokeAllForUser(@Param("userId") UUID userId, @Param("at") Instant at);

  @Query(
      value =
          """
          SELECT EXISTS (
              SELECT 1
                FROM user_sessions session
                JOIN users account ON account.user_id = session.user_id
               WHERE session.session_id = :sessionId
                 AND session.user_id = :userId
                 AND session.session_status = 'ACTIVE'
                 AND session.expires_at > :now
                 AND account.account_status = 'ACTIVE'
          )
          """,
      nativeQuery = true)
  boolean isActive(
      @Param("sessionId") UUID sessionId, @Param("userId") UUID userId, @Param("now") Instant now);
}
