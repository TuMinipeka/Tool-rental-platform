package com.toolrental.application.invoiceitem.usecase;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.invoiceitem.dto.InvoiceItemResponse;
import com.toolrental.application.invoiceitem.exception.InvoiceItemNotFoundApplicationException;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.invoice.port.repository.InvoiceRepository;
import com.toolrental.domain.invoiceitem.model.aggregate.InvoiceItem;
import com.toolrental.domain.invoiceitem.model.valueobject.InvoiceItemId;
import com.toolrental.domain.invoiceitem.port.repository.InvoiceItemRepository;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

public final class ManageInvoiceItemUseCase {
  private final InvoiceItemRepository repository;
  private final UnitOfWork work;
  private final AccessPolicy access;
  private final WorkflowReferences references;
  private final Clock clock;
  private final InvoiceRepository invoices;

  public ManageInvoiceItemUseCase(
      InvoiceItemRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      InvoiceRepository invoices) {
    this.repository = repository;
    this.work = work;
    this.access = access;
    this.references = references;
    this.clock = clock;
    this.invoices = invoices;
  }

  private InvoiceItem load(InvoiceItemId id) {
    return repository.findById(id).orElseThrow(InvoiceItemNotFoundApplicationException::new);
  }

  public InvoiceItemResponse find(UUID actor, InvoiceItemId id) {
    return work.execute(
        () -> {
          var value = load(id);
          var invoice =
              invoices
                  .findById(value.id().invoiceId())
                  .orElseThrow(() -> new InvoiceItemNotFoundApplicationException());
          references.reservation(actor, invoice.reservationId(), false);
          return InvoiceItemResponse.from(value);
        });
  }

  public List<InvoiceItemResponse> list(UUID actor) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          return repository.findAll().stream().map(InvoiceItemResponse::from).toList();
        });
  }
}
