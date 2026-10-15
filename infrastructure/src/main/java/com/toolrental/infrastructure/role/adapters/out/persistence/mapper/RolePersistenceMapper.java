package com.toolrental.infrastructure.role.adapters.out.persistence.mapper;

import com.toolrental.domain.role.model.aggregate.Role;
import com.toolrental.domain.role.model.valueobject.RoleId;
import com.toolrental.infrastructure.role.adapters.out.persistence.entity.RoleJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class RolePersistenceMapper {
  public Short key(RoleId id) {
    return id.value();
  }

  public Role toDomain(RoleJpaEntity row) {
    return Role.restore(new RoleId(row.getId()), row.getCode(), row.getDescription());
  }

  public RoleJpaEntity toEntity(Role value) {
    return new RoleJpaEntity(key(value.id()), value.code(), value.description());
  }
}
