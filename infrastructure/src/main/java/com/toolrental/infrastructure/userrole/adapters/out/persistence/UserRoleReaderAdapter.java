package com.toolrental.infrastructure.userrole.adapters.out.persistence;

import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.domain.user.port.UserRoleReader;
import com.toolrental.infrastructure.userrole.adapters.out.persistence.repository.UserRoleJpaRepository;
import java.util.Set;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class UserRoleReaderAdapter implements UserRoleReader {
  private final UserRoleJpaRepository rows;

  public UserRoleReaderAdapter(UserRoleJpaRepository rows) {
    this.rows = rows;
  }

  public Set<String> findRoleCodes(UserId userId) {
    return Set.copyOf(rows.findRoleCodesByUserId(userId.value()));
  }
}
