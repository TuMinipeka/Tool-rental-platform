package com.toolrental.application.invoice.usecase;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.invoice.dto.InvoiceDocumentResponse;
import com.toolrental.application.invoice.dto.InvoiceResponse;
import com.toolrental.application.invoice.exception.InvoiceNotFoundApplicationException;
import com.toolrental.application.invoiceitem.dto.InvoiceItemResponse;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.invoice.model.valueobject.InvoiceId;
import com.toolrental.domain.invoice.port.repository.InvoiceRepository;
import com.toolrental.domain.invoiceitem.port.repository.InvoiceItemRepository;
import java.math.BigDecimal;
import java.util.UUID;

public final class DownloadInvoiceUseCase {
  public static final String LEGAL_NOTICE =
      "Documento académico - no válido como factura ante la DIAN";

  private final InvoiceRepository invoices;
  private final InvoiceItemRepository lines;
  private final WorkflowReferences references;
  private final UnitOfWork work;

  public DownloadInvoiceUseCase(
      InvoiceRepository invoices,
      InvoiceItemRepository lines,
      WorkflowReferences references,
      UnitOfWork work) {
    this.invoices = invoices;
    this.lines = lines;
    this.references = references;
    this.work = work;
  }

  public InvoiceDocumentResponse execute(UUID actor, InvoiceId id) {
    return work.execute(
        () -> {
          var invoice = invoices.findById(id).orElseThrow(InvoiceNotFoundApplicationException::new);
          var reservation = references.reservation(actor, invoice.reservationId(), false);
          var invoiceLines = lines.findByInvoice(id);
          var total =
              invoiceLines.stream()
                  .map(line -> line.quantity().multiply(line.issuedUnitPrice()))
                  .reduce(BigDecimal.ZERO, BigDecimal::add);
          return new InvoiceDocumentResponse(
              InvoiceResponse.from(invoice),
              invoiceLines.stream().map(InvoiceItemResponse::from).toList(),
              total,
              reservation.total().currency().getCurrencyCode(),
              LEGAL_NOTICE);
        });
  }
}
