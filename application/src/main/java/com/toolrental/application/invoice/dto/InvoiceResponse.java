package com.toolrental.application.invoice.dto;

import com.toolrental.domain.invoice.model.aggregate.Invoice;
import java.time.Instant;
import java.util.UUID;

public record InvoiceResponse(
    UUID invoiceId,
    UUID reservationId,
    String number,
    Instant issuedAt,
    String invoiceStatus,
    String issuedSupplierName,
    String issuedSupplierDocument,
    String issuedCustomerName,
    String issuedCustomerDocument) {
  public static InvoiceResponse from(Invoice value) {
    return new InvoiceResponse(
        value.id().value(),
        value.reservationId().value(),
        value.number(),
        value.issuedAt(),
        value.invoiceStatus(),
        value.issuedSupplierName(),
        value.issuedSupplierDocument(),
        value.issuedCustomerName(),
        value.issuedCustomerDocument());
  }
}
