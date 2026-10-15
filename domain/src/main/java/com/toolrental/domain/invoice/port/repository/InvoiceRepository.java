package com.toolrental.domain.invoice.port.repository;

import com.toolrental.domain.invoice.model.aggregate.Invoice;
import com.toolrental.domain.invoice.model.valueobject.InvoiceId;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository {
  Optional<Invoice> findById(InvoiceId id);

  List<Invoice> findAll();

  Invoice save(Invoice value);

  void delete(InvoiceId id);
}
