package com.toolrental.infrastructure.user.adapters.out.persistence;

import com.toolrental.domain.user.model.aggregate.User;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.domain.user.port.repository.UserRepository;
import com.toolrental.infrastructure.user.adapters.out.persistence.mapper.UserPersistenceMapper;
import com.toolrental.infrastructure.user.adapters.out.persistence.repository.UserJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class UserPersistenceAdapter implements UserRepository {
  private final UserJpaRepository rows;
  private final UserPersistenceMapper mapper;

  public UserPersistenceAdapter(UserJpaRepository rows, UserPersistenceMapper mapper) {
    this.rows = rows;
    this.mapper = mapper;
  }

  public Optional<User> findById(UserId id) {
    return rows.findLocked(mapper.key(id)).map(mapper::toDomain);
  }

  public List<User> findAll() {
    return rows.findAll().stream().map(mapper::toDomain).toList();
  }

  public User save(User value) {
    return mapper.toDomain(rows.saveAndFlush(mapper.toEntity(value)));
  }

  public void delete(UserId id) {
    rows.deleteById(mapper.key(id));
    rows.flush();
  }
}
