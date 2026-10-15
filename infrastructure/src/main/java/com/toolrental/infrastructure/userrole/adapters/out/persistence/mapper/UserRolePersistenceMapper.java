package com.toolrental.infrastructure.userrole.adapters.out.persistence.mapper;

import com.toolrental.domain.role.model.valueobject.RoleId;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.domain.userrole.model.aggregate.UserRole;
import com.toolrental.domain.userrole.model.valueobject.UserRoleId;
import com.toolrental.infrastructure.userrole.adapters.out.persistence.entity.UserRoleJpaEntity;
import com.toolrental.infrastructure.userrole.adapters.out.persistence.entity.UserRoleJpaId;
import org.springframework.stereotype.Component;

@Component
public class UserRolePersistenceMapper {
  public UserRoleJpaId key(UserRoleId id) {
    return new UserRoleJpaId(id.userId().value(), id.roleId().value());
  }

  public UserRole toDomain(UserRoleJpaEntity row) {
    return UserRole.restore(
        new UserRoleId(new UserId(row.getId().getUserId()), new RoleId(row.getId().getRoleId())));
  }

  public UserRoleJpaEntity toEntity(UserRole value) {
    return new UserRoleJpaEntity(key(value.id()));
  }
}
