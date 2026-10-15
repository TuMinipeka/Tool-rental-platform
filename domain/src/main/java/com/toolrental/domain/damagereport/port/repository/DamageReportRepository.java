package com.toolrental.domain.damagereport.port.repository;

import com.toolrental.domain.damagereport.model.aggregate.DamageReport;
import com.toolrental.domain.damagereport.model.valueobject.DamageReportId;
import java.util.List;
import java.util.Optional;

public interface DamageReportRepository {
  Optional<DamageReport> findById(DamageReportId id);

  List<DamageReport> findAll();

  DamageReport save(DamageReport value);

  void delete(DamageReportId id);
}
