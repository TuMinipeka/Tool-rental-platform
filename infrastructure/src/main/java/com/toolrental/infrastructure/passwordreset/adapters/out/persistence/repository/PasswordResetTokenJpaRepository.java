package com.toolrental.infrastructure.passwordreset.adapters.out.persistence.repository;

import com.toolrental.infrastructure.passwordreset.adapters.out.persistence.entity.PasswordResetTokenJpaEntity;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordResetTokenJpaRepository
    extends JpaRepository<PasswordResetTokenJpaEntity, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select token from PasswordResetTokenJpaEntity token where token.tokenHash = :hash")
  Optional<PasswordResetTokenJpaEntity> findByTokenHashForUpdate(@Param("hash") String hash);

  @Modifying
  @Query(
      "update PasswordResetTokenJpaEntity token set token.revokedAt = :at "
          + "where token.userId = :userId and token.usedAt is null and token.revokedAt is null")
  int revokeUnusedForUser(@Param("userId") UUID userId, @Param("at") Instant at);
}
