package com.toolrental.infrastructure.unitreturn.adapters.out.persistence;

import com.toolrental.domain.unitreturn.model.aggregate.UnitReturn;
import com.toolrental.domain.unitreturn.model.valueobject.UnitReturnId;
import com.toolrental.domain.unitreturn.port.repository.UnitReturnRepository;
import com.toolrental.infrastructure.unitreturn.adapters.out.persistence.mapper.UnitReturnPersistenceMapper;
import com.toolrental.infrastructure.unitreturn.adapters.out.persistence.repository.UnitReturnJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class UnitReturnPersistenceAdapter implements UnitReturnRepository {
  private final UnitReturnJpaRepository rows;
  private final UnitReturnPersistenceMapper mapper;

  public UnitReturnPersistenceAdapter(
      UnitReturnJpaRepository rows, UnitReturnPersistenceMapper mapper) {
    this.rows = rows;
    this.mapper = mapper;
  }

  public Optional<UnitReturn> findById(UnitReturnId id) {
    return rows.findLocked(mapper.key(id)).map(mapper::toDomain);
  }

  public List<UnitReturn> findAll() {
    return rows.findAll().stream().map(mapper::toDomain).toList();
  }

  public UnitReturn save(UnitReturn value) {
    return mapper.toDomain(rows.saveAndFlush(mapper.toEntity(value)));
  }

  public void delete(UnitReturnId id) {
    rows.deleteById(mapper.key(id));
    rows.flush();
  }
}
