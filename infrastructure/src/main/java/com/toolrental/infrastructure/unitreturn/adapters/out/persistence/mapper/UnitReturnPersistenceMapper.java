package com.toolrental.infrastructure.unitreturn.adapters.out.persistence.mapper;

import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.unitreturn.model.aggregate.UnitReturn;
import com.toolrental.domain.unitreturn.model.valueobject.UnitReturnId;
import com.toolrental.infrastructure.unitreturn.adapters.out.persistence.entity.UnitReturnJpaEntity;
import com.toolrental.infrastructure.unitreturn.adapters.out.persistence.entity.UnitReturnJpaId;
import java.util.Currency;
import org.springframework.stereotype.Component;

@Component
public class UnitReturnPersistenceMapper {
  public UnitReturnJpaId key(UnitReturnId id) {
    return new UnitReturnJpaId(id.reservationId().value(), id.equipmentUnitId().value());
  }

  public UnitReturn toDomain(UnitReturnJpaEntity row) {
    return UnitReturn.restore(
        new UnitReturnId(
            new ReservationId(row.getId().getReservationId()),
            new EquipmentUnitId(row.getId().getEquipmentUnitId())),
        row.getReceivedAt(),
        row.getInboundCondition(),
        row.getNotes(),
        row.getLateDays(),
        new Money(row.getLateFee(), Currency.getInstance(row.getLateFeeCurrency())));
  }

  public UnitReturnJpaEntity toEntity(UnitReturn value) {
    return new UnitReturnJpaEntity(
        key(value.id()),
        value.receivedAt(),
        value.inboundCondition(),
        value.notes(),
        value.lateDays(),
        value.lateFee().amount(),
        value.lateFee().currency().getCurrencyCode());
  }
}
