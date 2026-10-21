package com.toolrental.infrastructure.invoiceitem.adapters.out.persistence.mapper;

import com.toolrental.domain.invoice.model.valueobject.InvoiceId;
import com.toolrental.domain.invoiceitem.model.aggregate.InvoiceItem;
import com.toolrental.domain.invoiceitem.model.valueobject.InvoiceItemId;
import com.toolrental.infrastructure.invoiceitem.adapters.out.persistence.entity.InvoiceItemJpaEntity;
import com.toolrental.infrastructure.invoiceitem.adapters.out.persistence.entity.InvoiceItemJpaId;
import org.springframework.stereotype.Component;

@Component
public class InvoiceItemPersistenceMapper {
  public InvoiceItemJpaId key(InvoiceItemId id) {
    return new InvoiceItemJpaId(id.invoiceId().value(), id.lineNumber());
  }

  public InvoiceItem toDomain(InvoiceItemJpaEntity row) {
    return InvoiceItem.restore(
        new InvoiceItemId(new InvoiceId(row.getId().getInvoiceId()), row.getId().getLineNumber()),
        row.getIssuedDescription(),
        row.getQuantity(),
        row.getIssuedUnitPrice());
  }

  public InvoiceItemJpaEntity toEntity(InvoiceItem value) {
    return new InvoiceItemJpaEntity(
        key(value.id()), value.issuedDescription(), value.quantity(), value.issuedUnitPrice());
  }
}
