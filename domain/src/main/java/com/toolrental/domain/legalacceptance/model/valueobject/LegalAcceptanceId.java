package com.toolrental.domain.legalacceptance.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record LegalAcceptanceId(UUID value) {
  public LegalAcceptanceId {
    Objects.requireNonNull(value);
  }

  public static LegalAcceptanceId generate() {
    return new LegalAcceptanceId(UUID.randomUUID());
  }
}
