package com.toolrental.application.reservation.command;

import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.valueobject.FulfillmentMethod;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import java.util.List;

public record QuoteReservationCommand(
    ReservationPeriod period,
    List<EquipmentUnitId> equipmentUnitIds,
    FulfillmentMethod fulfillmentMethod,
    String deliveryCountryCode) {
  public QuoteReservationCommand {
    equipmentUnitIds = List.copyOf(equipmentUnitIds);
    if (fulfillmentMethod == null) fulfillmentMethod = FulfillmentMethod.CUSTOMER_PICKUP;
  }

  public QuoteReservationCommand(ReservationPeriod period, List<EquipmentUnitId> ids) {
    this(period, ids, FulfillmentMethod.CUSTOMER_PICKUP, null);
  }
}
