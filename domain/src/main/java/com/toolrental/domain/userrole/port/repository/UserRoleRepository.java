package com.toolrental.domain.userrole.port.repository;

import com.toolrental.domain.userrole.model.aggregate.UserRole;
import com.toolrental.domain.userrole.model.valueobject.UserRoleId;
import java.util.List;
import java.util.Optional;

public interface UserRoleRepository {
  Optional<UserRole> findById(UserRoleId id);

  List<UserRole> findAll();

  UserRole save(UserRole value);

  void delete(UserRoleId id);
}
