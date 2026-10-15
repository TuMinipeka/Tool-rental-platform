package com.toolrental.domain.invoice.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record InvoiceId(UUID value) {
  public InvoiceId {
    Objects.requireNonNull(value);
  }

  public static InvoiceId generate() {
    return new InvoiceId(UUID.randomUUID());
  }
}
