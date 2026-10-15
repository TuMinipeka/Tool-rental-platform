package com.toolrental.infrastructure.userrole.adapters.out.persistence;

import com.toolrental.domain.userrole.model.aggregate.UserRole;
import com.toolrental.domain.userrole.model.valueobject.UserRoleId;
import com.toolrental.domain.userrole.port.repository.UserRoleRepository;
import com.toolrental.infrastructure.userrole.adapters.out.persistence.mapper.UserRolePersistenceMapper;
import com.toolrental.infrastructure.userrole.adapters.out.persistence.repository.UserRoleJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class UserRolePersistenceAdapter implements UserRoleRepository {
  private final UserRoleJpaRepository rows;
  private final UserRolePersistenceMapper mapper;

  public UserRolePersistenceAdapter(UserRoleJpaRepository rows, UserRolePersistenceMapper mapper) {
    this.rows = rows;
    this.mapper = mapper;
  }

  public Optional<UserRole> findById(UserRoleId id) {
    return rows.findLocked(mapper.key(id)).map(mapper::toDomain);
  }

  public List<UserRole> findAll() {
    return rows.findAll().stream().map(mapper::toDomain).toList();
  }

  public UserRole save(UserRole value) {
    return mapper.toDomain(rows.saveAndFlush(mapper.toEntity(value)));
  }

  public void delete(UserRoleId id) {
    rows.deleteById(mapper.key(id));
    rows.flush();
  }
}
