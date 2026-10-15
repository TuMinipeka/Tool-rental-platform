package com.toolrental.infrastructure.damagereport.adapters.out.persistence.mapper;

import com.toolrental.domain.damagereport.model.aggregate.DamageReport;
import com.toolrental.domain.damagereport.model.valueobject.DamageReportId;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.infrastructure.damagereport.adapters.out.persistence.entity.DamageReportJpaEntity;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DamageReportPersistenceMapper {
  public UUID key(DamageReportId id) {
    return id.value();
  }

  public DamageReport toDomain(DamageReportJpaEntity row) {
    return DamageReport.restore(
        new DamageReportId(row.getId()),
        new ReservationId(row.getReservationId()),
        new EquipmentUnitId(row.getEquipmentUnitId()),
        row.getReportedAt(),
        row.getDescription(),
        row.getReportStatus(),
        row.getAssessedCost());
  }

  public DamageReportJpaEntity toEntity(DamageReport value) {
    return new DamageReportJpaEntity(
        key(value.id()),
        value.reservationId().value(),
        value.equipmentUnitId().value(),
        value.reportedAt(),
        value.description(),
        value.reportStatus(),
        value.assessedCost());
  }
}
