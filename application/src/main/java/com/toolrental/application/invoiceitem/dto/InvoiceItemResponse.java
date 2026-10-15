package com.toolrental.application.invoiceitem.dto;

import com.toolrental.domain.invoiceitem.model.aggregate.InvoiceItem;
import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceItemResponse(
    UUID invoiceId,
    Integer lineNumber,
    String issuedDescription,
    BigDecimal quantity,
    BigDecimal issuedUnitPrice) {
  public static InvoiceItemResponse from(InvoiceItem value) {
    return new InvoiceItemResponse(
        value.id().invoiceId().value(),
        value.id().lineNumber(),
        value.issuedDescription(),
        value.quantity(),
        value.issuedUnitPrice());
  }
}
