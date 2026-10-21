package com.toolrental.domain.invoiceitem.port.repository;

import com.toolrental.domain.invoice.model.valueobject.InvoiceId;
import com.toolrental.domain.invoiceitem.model.aggregate.InvoiceItem;
import com.toolrental.domain.invoiceitem.model.valueobject.InvoiceItemId;
import java.util.List;
import java.util.Optional;

public interface InvoiceItemRepository {
  Optional<InvoiceItem> findById(InvoiceItemId id);

  List<InvoiceItem> findAll();

  List<InvoiceItem> findByInvoice(InvoiceId invoiceId);

  InvoiceItem save(InvoiceItem value);

  void delete(InvoiceItemId id);
}
