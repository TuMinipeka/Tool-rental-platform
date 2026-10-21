package com.toolrental.infrastructure.role.adapters.out.persistence;

import com.toolrental.domain.role.model.aggregate.Role;
import com.toolrental.domain.role.model.valueobject.RoleId;
import com.toolrental.domain.role.port.repository.RoleRepository;
import com.toolrental.infrastructure.role.adapters.out.persistence.mapper.RolePersistenceMapper;
import com.toolrental.infrastructure.role.adapters.out.persistence.repository.RoleJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class RolePersistenceAdapter implements RoleRepository {
  private final RoleJpaRepository rows;
  private final RolePersistenceMapper mapper;

  public RolePersistenceAdapter(RoleJpaRepository rows, RolePersistenceMapper mapper) {
    this.rows = rows;
    this.mapper = mapper;
  }

  public Optional<Role> findById(RoleId id) {
    return rows.findLocked(mapper.key(id)).map(mapper::toDomain);
  }

  public Optional<Role> findByCode(String code) {
    return rows.findByCode(code).map(mapper::toDomain);
  }

  public List<Role> findAll() {
    return rows.findAll().stream().map(mapper::toDomain).toList();
  }

  public Role save(Role value) {
    return mapper.toDomain(rows.saveAndFlush(mapper.toEntity(value)));
  }

  public void delete(RoleId id) {
    rows.deleteById(mapper.key(id));
    rows.flush();
  }
}
