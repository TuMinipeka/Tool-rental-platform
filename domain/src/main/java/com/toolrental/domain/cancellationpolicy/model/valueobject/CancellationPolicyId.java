package com.toolrental.domain.cancellationpolicy.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record CancellationPolicyId(UUID value) {
  public CancellationPolicyId {
    Objects.requireNonNull(value);
  }

  public static CancellationPolicyId generate() {
    return new CancellationPolicyId(UUID.randomUUID());
  }
}
