package com.toolrental.infrastructure.usersession.adapters.out.persistence.mapper;

import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.domain.usersession.model.aggregate.UserSession;
import com.toolrental.domain.usersession.model.valueobject.UserSessionId;
import com.toolrental.infrastructure.usersession.adapters.out.persistence.entity.UserSessionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserSessionPersistenceMapper {
  public UserSession toDomain(UserSessionJpaEntity entity) {
    return UserSession.restore(
        new UserSessionId(entity.getId()),
        entity.getFamilyId(),
        new UserId(entity.getUserId()),
        entity.getRefreshTokenHash(),
        entity.getStatus(),
        entity.getCreatedAt(),
        entity.getExpiresAt(),
        entity.getRotatedAt(),
        entity.getRevokedAt(),
        entity.getReplacedBySessionId() == null
            ? null
            : new UserSessionId(entity.getReplacedBySessionId()),
        entity.getClientLabel());
  }

  public UserSessionJpaEntity toEntity(UserSession session) {
    return new UserSessionJpaEntity(
        session.id().value(),
        session.familyId(),
        session.userId().value(),
        session.refreshTokenHash(),
        session.status(),
        session.createdAt(),
        session.expiresAt(),
        session.rotatedAt(),
        session.revokedAt(),
        session.replacedBy() == null ? null : session.replacedBy().value(),
        session.clientLabel());
  }
}
