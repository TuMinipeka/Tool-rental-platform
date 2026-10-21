package com.toolrental.infrastructure.passwordreset.adapters.out.persistence;

import com.toolrental.domain.passwordreset.model.aggregate.PasswordResetToken;
import com.toolrental.domain.passwordreset.port.repository.PasswordResetTokenRepository;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.infrastructure.passwordreset.adapters.out.persistence.mapper.PasswordResetTokenPersistenceMapper;
import com.toolrental.infrastructure.passwordreset.adapters.out.persistence.repository.PasswordResetTokenJpaRepository;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class PasswordResetTokenPersistenceAdapter implements PasswordResetTokenRepository {
  private final PasswordResetTokenJpaRepository rows;
  private final PasswordResetTokenPersistenceMapper mapper;

  public PasswordResetTokenPersistenceAdapter(
      PasswordResetTokenJpaRepository rows, PasswordResetTokenPersistenceMapper mapper) {
    this.rows = rows;
    this.mapper = mapper;
  }

  public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
    return rows.findByTokenHashForUpdate(tokenHash).map(mapper::toDomain);
  }

  public PasswordResetToken save(PasswordResetToken token) {
    return mapper.toDomain(rows.saveAndFlush(mapper.toEntity(token)));
  }

  public void revokeUnusedForUser(UserId userId, Instant revokedAt) {
    rows.revokeUnusedForUser(userId.value(), revokedAt);
    rows.flush();
  }
}
