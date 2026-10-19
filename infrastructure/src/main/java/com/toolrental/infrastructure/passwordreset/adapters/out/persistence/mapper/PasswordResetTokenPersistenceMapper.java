package com.toolrental.infrastructure.passwordreset.adapters.out.persistence.mapper;

import com.toolrental.domain.passwordreset.model.aggregate.PasswordResetToken;
import com.toolrental.domain.passwordreset.model.valueobject.PasswordResetTokenId;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.infrastructure.passwordreset.adapters.out.persistence.entity.PasswordResetTokenJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetTokenPersistenceMapper {
  public PasswordResetToken toDomain(PasswordResetTokenJpaEntity entity) {
    return PasswordResetToken.restore(
        new PasswordResetTokenId(entity.getId()),
        new UserId(entity.getUserId()),
        entity.getTokenHash(),
        entity.getCreatedAt(),
        entity.getExpiresAt(),
        entity.getUsedAt(),
        entity.getRevokedAt());
  }

  public PasswordResetTokenJpaEntity toEntity(PasswordResetToken token) {
    return new PasswordResetTokenJpaEntity(
        token.id().value(),
        token.userId().value(),
        token.tokenHash(),
        token.createdAt(),
        token.expiresAt(),
        token.usedAt(),
        token.revokedAt());
  }
}
