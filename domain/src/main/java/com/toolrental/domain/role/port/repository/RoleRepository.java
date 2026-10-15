package com.toolrental.domain.role.port.repository;

import com.toolrental.domain.role.model.aggregate.Role;
import com.toolrental.domain.role.model.valueobject.RoleId;
import java.util.List;
import java.util.Optional;

public interface RoleRepository {
  Optional<Role> findById(RoleId id);

  List<Role> findAll();

  Role save(Role value);

  void delete(RoleId id);
}
