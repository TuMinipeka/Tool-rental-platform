package com.toolrental.infrastructure.reservation.adapters.out.persistence.mapper;

import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.customer.model.valueobject.CustomerId;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.aggregate.Reservation;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import com.toolrental.domain.reservationitem.model.entity.ReservationLine;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import com.toolrental.infrastructure.reservation.adapters.out.persistence.entity.ReservationJpaEntity;
import com.toolrental.infrastructure.reservationitem.adapters.out.persistence.entity.ReservationItemJpaEntity;
import com.toolrental.infrastructure.reservationitem.adapters.out.persistence.entity.ReservationItemJpaId;
import java.util.Currency;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ReservationPersistenceMapper {
  public Reservation toDomain(
      ReservationJpaEntity entity, Map<UUID, UUID> supplierByEquipmentUnit) {
    var currency = Currency.getInstance(entity.getCurrency());
    var items =
        entity.getItems().stream()
            .map(
                item ->
                    new ReservationLine(
                        new EquipmentUnitId(item.getEquipmentUnitId()),
                        new SupplierId(
                            requireSupplier(item.getEquipmentUnitId(), supplierByEquipmentUnit)),
                        new Money(item.getAgreedDailyRate(), currency)))
            .toList();
    return Reservation.restore(
        new ReservationId(entity.getId()),
        new CustomerId(entity.getCustomerId()),
        new ReservationPeriod(entity.getStartDate(), entity.getReturnDate()),
        items,
        entity.getCreatedAt(),
        ReservationStatus.valueOf(entity.getReservationStatus()),
        entity.getConfirmedAt(),
        entity.getExpiresAt());
  }

  public ReservationJpaEntity toEntity(Reservation aggregate) {
    var items =
        aggregate.items().stream()
            .map(
                item ->
                    new ReservationItemJpaEntity(
                        new ReservationItemJpaId(
                            aggregate.id().value(), item.equipmentUnitId().value()),
                        item.dailyRate().amount()))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    var entity =
        new ReservationJpaEntity(
            aggregate.id().value(),
            aggregate.customerId().value(),
            aggregate.createdAt(),
            aggregate.period().startDate(),
            aggregate.period().returnDate(),
            aggregate.status().name(),
            aggregate.items().getFirst().dailyRate().currency().getCurrencyCode(),
            aggregate.confirmation().map(event -> event.occurredOn()).orElse(null),
            items);
    entity.preserveExpiresAt(aggregate.expiresAt());
    return entity;
  }

  private UUID requireSupplier(UUID equipmentUnitId, Map<UUID, UUID> supplierByEquipmentUnit) {
    var supplierId = supplierByEquipmentUnit.get(equipmentUnitId);
    if (supplierId == null) {
      throw new IllegalStateException("No supplier found for equipment unit " + equipmentUnitId);
    }
    return supplierId;
  }
}
