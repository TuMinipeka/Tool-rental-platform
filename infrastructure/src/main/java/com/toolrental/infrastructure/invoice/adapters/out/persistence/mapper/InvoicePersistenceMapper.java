package com.toolrental.infrastructure.invoice.adapters.out.persistence.mapper;

import com.toolrental.domain.invoice.model.aggregate.Invoice;
import com.toolrental.domain.invoice.model.valueobject.InvoiceId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.infrastructure.invoice.adapters.out.persistence.entity.InvoiceJpaEntity;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class InvoicePersistenceMapper {
  public UUID key(InvoiceId id) {
    return id.value();
  }

  public Invoice toDomain(InvoiceJpaEntity row) {
    return Invoice.restore(
        new InvoiceId(row.getId()),
        new ReservationId(row.getReservationId()),
        row.getNumber(),
        row.getIssuedAt(),
        row.getInvoiceStatus(),
        row.getIssuedSupplierName(),
        row.getIssuedSupplierDocument(),
        row.getIssuedCustomerName(),
        row.getIssuedCustomerDocument());
  }

  public InvoiceJpaEntity toEntity(Invoice value) {
    return new InvoiceJpaEntity(
        key(value.id()),
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
