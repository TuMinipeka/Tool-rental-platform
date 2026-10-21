package com.toolrental.domain.deliveryrate.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record DeliveryRateId(UUID value) {
  public DeliveryRateId {
    Objects.requireNonNull(value);
  }

  public static DeliveryRateId generate() {
    return new DeliveryRateId(UUID.randomUUID());
  }
}
