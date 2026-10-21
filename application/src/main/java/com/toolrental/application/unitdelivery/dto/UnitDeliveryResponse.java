package com.toolrental.application.unitdelivery.dto;

import com.toolrental.domain.unitdelivery.model.aggregate.UnitDelivery;
import java.time.Instant;
import java.util.UUID;

public record UnitDeliveryResponse(
    UUID reservationId,
    UUID equipmentUnitId,
    Instant scheduledAt,
    Instant deliveredAt,
    String outboundCondition,
    String addressLine,
    String city,
    String region,
    String country,
    String fulfillmentMethod,
    String deliveryStatus,
    String failureType,
    String failureReason,
    Instant failedAt) {
  public static UnitDeliveryResponse from(UnitDelivery value) {
    return new UnitDeliveryResponse(
        value.id().reservationId().value(),
        value.id().equipmentUnitId().value(),
        value.scheduledAt(),
        value.deliveredAt(),
        value.outboundCondition(),
        value.addressLine(),
        value.city(),
        value.region(),
        value.country(),
        value.fulfillmentMethod(),
        value.deliveryStatus(),
        value.failureType(),
        value.failureReason(),
        value.failedAt());
  }
}
