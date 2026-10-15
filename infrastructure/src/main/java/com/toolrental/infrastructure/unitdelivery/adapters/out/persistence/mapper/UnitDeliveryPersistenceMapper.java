package com.toolrental.infrastructure.unitdelivery.adapters.out.persistence.mapper;

import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.unitdelivery.model.aggregate.UnitDelivery;
import com.toolrental.domain.unitdelivery.model.valueobject.UnitDeliveryId;
import com.toolrental.infrastructure.unitdelivery.adapters.out.persistence.entity.UnitDeliveryJpaEntity;
import com.toolrental.infrastructure.unitdelivery.adapters.out.persistence.entity.UnitDeliveryJpaId;
import org.springframework.stereotype.Component;

@Component
public class UnitDeliveryPersistenceMapper {
  public UnitDeliveryJpaId key(UnitDeliveryId id) {
    return new UnitDeliveryJpaId(id.reservationId().value(), id.equipmentUnitId().value());
  }

  public UnitDelivery toDomain(UnitDeliveryJpaEntity row) {
    return UnitDelivery.restore(
        new UnitDeliveryId(
            new ReservationId(row.getId().getReservationId()),
            new EquipmentUnitId(row.getId().getEquipmentUnitId())),
        row.getScheduledAt(),
        row.getDeliveredAt(),
        row.getOutboundCondition(),
        row.getAddressLine(),
        row.getCity(),
        row.getRegion(),
        row.getCountry());
  }

  public UnitDeliveryJpaEntity toEntity(UnitDelivery value) {
    return new UnitDeliveryJpaEntity(
        key(value.id()),
        value.scheduledAt(),
        value.deliveredAt(),
        value.outboundCondition(),
        value.addressLine(),
        value.city(),
        value.region(),
        value.country());
  }
}
