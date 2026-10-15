package com.toolrental.infrastructure.reservationitem.adapters.out.persistence.mapper;

import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservationitem.model.aggregate.ReservationItem;
import com.toolrental.domain.reservationitem.model.valueobject.ReservationItemId;
import com.toolrental.infrastructure.reservationitem.adapters.out.persistence.entity.ReservationItemJpaEntity;
import com.toolrental.infrastructure.reservationitem.adapters.out.persistence.entity.ReservationItemJpaId;
import org.springframework.stereotype.Component;

@Component
public class ReservationItemPersistenceMapper {
  public ReservationItemJpaId key(ReservationItemId id) {
    return new ReservationItemJpaId(id.reservationId().value(), id.equipmentUnitId().value());
  }

  public ReservationItem toDomain(ReservationItemJpaEntity row) {
    return ReservationItem.restore(
        new ReservationItemId(
            new ReservationId(row.getId().getReservationId()),
            new EquipmentUnitId(row.getId().getEquipmentUnitId())),
        row.getAgreedDailyRate());
  }

  public ReservationItemJpaEntity toEntity(ReservationItem value) {
    return new ReservationItemJpaEntity(key(value.id()), value.agreedDailyRate());
  }
}
