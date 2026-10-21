package com.toolrental.application.reservation.command;

import com.toolrental.domain.customer.model.valueobject.CustomerId;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.valueobject.DeliveryAddress;
import com.toolrental.domain.reservation.model.valueobject.FulfillmentMethod;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

public record RequestReservationCommand(
    CustomerId customerId,
    ReservationPeriod period,
    List<EquipmentUnitId> equipmentUnits,
    Instant scheduledFulfillmentAt,
    FulfillmentMethod fulfillmentMethod,
    DeliveryAddress deliveryAddress) {
  public RequestReservationCommand {
    Objects.requireNonNull(customerId);
    Objects.requireNonNull(period);
    equipmentUnits = List.copyOf(equipmentUnits);
    Objects.requireNonNull(scheduledFulfillmentAt);
    Objects.requireNonNull(fulfillmentMethod);
  }

  public RequestReservationCommand(
      CustomerId customerId, ReservationPeriod period, List<EquipmentUnitId> equipmentUnits) {
    this(
        customerId,
        period,
        equipmentUnits,
        period.startDate().atTime(8, 0).atZone(ZoneId.of("America/Bogota")).toInstant(),
        FulfillmentMethod.CUSTOMER_PICKUP,
        null);
  }
}
