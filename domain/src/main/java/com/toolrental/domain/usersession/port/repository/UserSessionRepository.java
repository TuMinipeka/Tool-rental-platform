package com.toolrental.domain.usersession.port.repository;

import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.domain.usersession.model.aggregate.UserSession;
import com.toolrental.domain.usersession.model.valueobject.UserSessionId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository {
  Optional<UserSession> findByRefreshTokenHash(String tokenHash);

  Optional<UserSession> findById(UserSessionId id);

  List<UserSession> findByUser(UserId userId);

  UserSession save(UserSession session);

  void saveRotation(UserSession rotated, UserSession successor);

  void revokeFamily(UUID familyId, Instant revokedAt, String status);

  void revokeAllForUser(UserId userId, Instant revokedAt);

  boolean isActive(UserSessionId sessionId, UserId userId, Instant now);
}
