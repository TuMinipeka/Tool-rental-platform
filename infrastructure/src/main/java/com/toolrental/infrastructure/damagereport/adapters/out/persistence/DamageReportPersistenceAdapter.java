package com.toolrental.infrastructure.damagereport.adapters.out.persistence;

import com.toolrental.domain.damagereport.model.aggregate.DamageReport;
import com.toolrental.domain.damagereport.model.valueobject.DamageReportId;
import com.toolrental.domain.damagereport.port.repository.DamageReportRepository;
import com.toolrental.infrastructure.damagereport.adapters.out.persistence.mapper.DamageReportPersistenceMapper;
import com.toolrental.infrastructure.damagereport.adapters.out.persistence.repository.DamageReportJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class DamageReportPersistenceAdapter implements DamageReportRepository {
  private final DamageReportJpaRepository rows;
  private final DamageReportPersistenceMapper mapper;

  public DamageReportPersistenceAdapter(
      DamageReportJpaRepository rows, DamageReportPersistenceMapper mapper) {
    this.rows = rows;
    this.mapper = mapper;
  }

  public Optional<DamageReport> findById(DamageReportId id) {
    return rows.findLocked(mapper.key(id)).map(mapper::toDomain);
  }

  public List<DamageReport> findAll() {
    return rows.findAll().stream().map(mapper::toDomain).toList();
  }

  public DamageReport save(DamageReport value) {
    return mapper.toDomain(rows.saveAndFlush(mapper.toEntity(value)));
  }

  public void delete(DamageReportId id) {
    rows.deleteById(mapper.key(id));
    rows.flush();
  }
}
