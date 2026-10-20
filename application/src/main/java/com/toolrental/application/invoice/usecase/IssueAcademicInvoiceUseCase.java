package com.toolrental.application.invoice.usecase;

import com.toolrental.application.common.exception.ReferenceNotFoundApplicationException;
import com.toolrental.domain.customer.port.repository.CustomerRepository;
import com.toolrental.domain.invoice.model.aggregate.Invoice;
import com.toolrental.domain.invoice.model.valueobject.InvoiceId;
import com.toolrental.domain.invoice.port.repository.InvoiceRepository;
import com.toolrental.domain.invoiceitem.model.aggregate.InvoiceItem;
import com.toolrental.domain.invoiceitem.model.valueobject.InvoiceItemId;
import com.toolrental.domain.invoiceitem.port.repository.InvoiceItemRepository;
import com.toolrental.domain.reservation.model.aggregate.Reservation;
import com.toolrental.domain.supplier.port.repository.SupplierRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

public final class IssueAcademicInvoiceUseCase {
  private final InvoiceRepository invoices;
  private final InvoiceItemRepository lines;
  private final CustomerRepository customers;
  private final SupplierRepository suppliers;
  private final Clock clock;

  public IssueAcademicInvoiceUseCase(
      InvoiceRepository invoices,
      InvoiceItemRepository lines,
      CustomerRepository customers,
      SupplierRepository suppliers,
      Clock clock) {
    this.invoices = invoices;
    this.lines = lines;
    this.customers = customers;
    this.suppliers = suppliers;
    this.clock = clock;
  }

  public Invoice execute(Reservation reservation) {
    var existing = invoices.findByReservation(reservation.id());
    if (existing.isPresent()) return existing.get();
    var customer =
        customers
            .findById(reservation.customerId())
            .orElseThrow(() -> new ReferenceNotFoundApplicationException("Customer"));
    var supplier =
        suppliers
            .findById(reservation.supplierId())
            .orElseThrow(() -> new ReferenceNotFoundApplicationException("Supplier"));
    var invoiceId = InvoiceId.generate();
    var invoice =
        invoices.save(
            Invoice.register(
                invoiceId,
                reservation.id(),
                "ACADEMIC-" + invoiceId.value(),
                Instant.now(clock),
                "PAID",
                supplier.businessName(),
                supplier.documentType() + " " + supplier.documentNumber(),
                customer.billingName(),
                customer.documentType() + " " + customer.documentNumber(),
                clock));
    var lineNumber = 1;
    for (var item : reservation.items()) {
      lines.save(
          InvoiceItem.register(
              new InvoiceItemId(invoice.id(), lineNumber++),
              "Academic rental of equipment unit " + item.equipmentUnitId().value(),
              BigDecimal.valueOf(reservation.period().billableDays()),
              item.dailyRate().amount(),
              clock));
    }
    if (reservation.deliveryFee().amount().signum() > 0) {
      lines.save(
          InvoiceItem.register(
              new InvoiceItemId(invoice.id(), lineNumber),
              "Supplier delivery",
              BigDecimal.ONE,
              reservation.deliveryFee().amount(),
              clock));
    }
    return invoice;
  }
}
