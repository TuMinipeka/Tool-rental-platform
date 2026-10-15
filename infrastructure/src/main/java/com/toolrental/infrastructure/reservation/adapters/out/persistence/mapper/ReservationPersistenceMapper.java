package com.toolrental.infrastructure.reservation.adapters.out.persistence.mapper;

import com.toolrental.domain.catalog.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.common.model.valueobject.CustomerId;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.common.model.valueobject.SupplierId;
import com.toolrental.domain.reservation.model.aggregate.Reservation;
import com.toolrental.domain.reservation.model.entity.ReservationItem;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import com.toolrental.infrastructure.reservation.adapters.out.persistence.entity.ReservationItemJpaEmbeddable;
import com.toolrental.infrastructure.reservation.adapters.out.persistence.entity.ReservationJpaEntity;
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
                    new ReservationItem(
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
        entity.getConfirmedAt());
  }

  public ReservationJpaEntity toEntity(Reservation aggregate) {
    var items =
        aggregate.items().stream()
            .map(
                item ->
                    new ReservationItemJpaEmbeddable(
                        item.equipmentUnitId().value(), item.dailyRate().amount()))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    return new ReservationJpaEntity(
        aggregate.id().value(),
        aggregate.customerId().value(),
        aggregate.createdAt(),
        aggregate.period().startDate(),
        aggregate.period().returnDate(),
        aggregate.status().name(),
        aggregate.items().getFirst().dailyRate().currency().getCurrencyCode(),
        aggregate.confirmation().map(event -> event.occurredOn()).orElse(null),
        items);
  }

  private UUID requireSupplier(UUID equipmentUnitId, Map<UUID, UUID> supplierByEquipmentUnit) {
    var supplierId = supplierByEquipmentUnit.get(equipmentUnitId);
    if (supplierId == null) {
      throw new IllegalStateException("No supplier found for equipment unit " + equipmentUnitId);
    }
    return supplierId;
  }
}
