package com.toolrental.domain.damagereport.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record DamageReportId(UUID value) {
  public DamageReportId {
    Objects.requireNonNull(value);
  }

  public static DamageReportId generate() {
    return new DamageReportId(UUID.randomUUID());
  }
}
