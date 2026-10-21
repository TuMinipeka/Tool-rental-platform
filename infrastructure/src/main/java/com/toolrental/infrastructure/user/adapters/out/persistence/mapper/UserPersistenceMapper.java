package com.toolrental.infrastructure.user.adapters.out.persistence.mapper;

import com.toolrental.domain.user.model.aggregate.User;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.infrastructure.user.adapters.out.persistence.entity.UserJpaEntity;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {
  public UUID key(UserId id) {
    return id.value();
  }

  public User toDomain(UserJpaEntity row) {
    return User.restore(
        new UserId(row.getId()),
        row.getEmail(),
        row.getDisplayName(),
        row.getPasswordHash(),
        row.getAccountStatus(),
        row.getRegisteredAt());
  }

  public UserJpaEntity toEntity(User value) {
    return new UserJpaEntity(
        key(value.id()),
        value.email(),
        value.displayName(),
        value.passwordHash(),
        value.accountStatus(),
        value.registeredAt());
  }
}
