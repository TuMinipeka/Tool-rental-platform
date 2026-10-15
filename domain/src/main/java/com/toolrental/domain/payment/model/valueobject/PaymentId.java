package com.toolrental.domain.payment.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record PaymentId(UUID value) {
  public PaymentId {
    Objects.requireNonNull(value);
  }

  public static PaymentId generate() {
    return new PaymentId(UUID.randomUUID());
  }
}
