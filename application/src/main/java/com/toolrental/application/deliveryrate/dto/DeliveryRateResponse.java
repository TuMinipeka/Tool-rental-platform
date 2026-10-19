package com.toolrental.application.deliveryrate.dto;

import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.deliveryrate.model.aggregate.DeliveryRate;
import com.toolrental.domain.deliveryrate.model.valueobject.DeliveryRateId;
import java.time.Instant;

public record DeliveryRateResponse(
    DeliveryRateId id, int version, Money price, boolean active, Instant effectiveAt) {
  public static DeliveryRateResponse from(DeliveryRate value) {
    return new DeliveryRateResponse(
        value.id(), value.version(), value.price(), value.active(), value.effectiveAt());
  }
}
