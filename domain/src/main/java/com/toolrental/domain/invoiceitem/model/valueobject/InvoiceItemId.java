package com.toolrental.domain.invoiceitem.model.valueobject;

import com.toolrental.domain.invoice.model.valueobject.InvoiceId;
import java.util.Objects;

public record InvoiceItemId(InvoiceId invoiceId, Integer lineNumber) {
  public InvoiceItemId {
    Objects.requireNonNull(invoiceId);
    Objects.requireNonNull(lineNumber);
  }
}
