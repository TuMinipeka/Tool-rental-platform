package com.toolrental.domain.refund.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record RefundId(UUID value) {
  public RefundId {
    Objects.requireNonNull(value);
  }

  public static RefundId generate() {
    return new RefundId(UUID.randomUUID());
  }
}
