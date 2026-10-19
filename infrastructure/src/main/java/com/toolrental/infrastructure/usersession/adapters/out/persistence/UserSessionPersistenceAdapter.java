package com.toolrental.infrastructure.usersession.adapters.out.persistence;

import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.domain.usersession.model.aggregate.UserSession;
import com.toolrental.domain.usersession.model.valueobject.UserSessionId;
import com.toolrental.domain.usersession.port.repository.UserSessionRepository;
import com.toolrental.infrastructure.usersession.adapters.out.persistence.mapper.UserSessionPersistenceMapper;
import com.toolrental.infrastructure.usersession.adapters.out.persistence.repository.UserSessionJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class UserSessionPersistenceAdapter implements UserSessionRepository {
  private final UserSessionJpaRepository rows;
  private final UserSessionPersistenceMapper mapper;

  public UserSessionPersistenceAdapter(
      UserSessionJpaRepository rows, UserSessionPersistenceMapper mapper) {
    this.rows = rows;
    this.mapper = mapper;
  }

  public Optional<UserSession> findByRefreshTokenHash(String tokenHash) {
    return rows.findByRefreshTokenHashForUpdate(tokenHash).map(mapper::toDomain);
  }

  public Optional<UserSession> findById(UserSessionId id) {
    return rows.findByIdForUpdate(id.value()).map(mapper::toDomain);
  }

  public List<UserSession> findByUser(UserId userId) {
    return rows.findByUserIdOrderByCreatedAtDesc(userId.value()).stream()
        .map(mapper::toDomain)
        .toList();
  }

  public UserSession save(UserSession session) {
    return mapper.toDomain(rows.saveAndFlush(mapper.toEntity(session)));
  }

  public void saveRotation(UserSession rotated, UserSession successor) {
    rows.saveAndFlush(mapper.toEntity(successor));
    rows.saveAndFlush(mapper.toEntity(rotated));
  }

  public void revokeFamily(UUID familyId, Instant revokedAt, String status) {
    rows.revokeFamily(familyId, revokedAt, status);
    rows.flush();
  }

  public void revokeAllForUser(UserId userId, Instant revokedAt) {
    rows.revokeAllForUser(userId.value(), revokedAt);
    rows.flush();
  }

  @Transactional(readOnly = true)
  public boolean isActive(UserSessionId sessionId, UserId userId, Instant now) {
    return rows.isActive(sessionId.value(), userId.value(), now);
  }
}
