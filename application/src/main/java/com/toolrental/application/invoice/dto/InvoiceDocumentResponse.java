package com.toolrental.application.invoice.dto;

import com.toolrental.application.invoiceitem.dto.InvoiceItemResponse;
import java.math.BigDecimal;
import java.util.List;

public record InvoiceDocumentResponse(
    InvoiceResponse invoice,
    List<InvoiceItemResponse> lines,
    BigDecimal total,
    String currency,
    String legalNotice) {
  public InvoiceDocumentResponse {
    lines = List.copyOf(lines);
  }
}
