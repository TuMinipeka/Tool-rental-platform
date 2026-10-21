package com.toolrental.application.invoice.usecase;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.invoice.command.RegisterInvoiceCommand;
import com.toolrental.application.invoice.dto.InvoiceResponse;
import com.toolrental.application.invoice.exception.InvoiceNotFoundApplicationException;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.customer.port.repository.CustomerRepository;
import com.toolrental.domain.invoice.model.aggregate.Invoice;
import com.toolrental.domain.invoice.model.valueobject.InvoiceId;
import com.toolrental.domain.invoice.port.repository.InvoiceRepository;
import com.toolrental.domain.invoiceitem.model.aggregate.InvoiceItem;
import com.toolrental.domain.invoiceitem.model.valueobject.InvoiceItemId;
import com.toolrental.domain.invoiceitem.port.repository.InvoiceItemRepository;
import com.toolrental.domain.rental.model.valueobject.RentalId;
import com.toolrental.domain.rental.port.repository.RentalRepository;
import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import com.toolrental.domain.supplier.port.repository.SupplierRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ManageInvoiceUseCase {
  private final InvoiceRepository repository;
  private final UnitOfWork work;
  private final AccessPolicy access;
  private final WorkflowReferences references;
  private final Clock clock;
  private final RentalRepository rentals;
  private final CustomerRepository customers;
  private final SupplierRepository suppliers;
  private final InvoiceItemRepository lines;

  public ManageInvoiceUseCase(
      InvoiceRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      RentalRepository rentals,
      CustomerRepository customers,
      SupplierRepository suppliers,
      InvoiceItemRepository lines) {
    this.repository = repository;
    this.work = work;
    this.access = access;
    this.references = references;
    this.clock = clock;
    this.rentals = rentals;
    this.customers = customers;
    this.suppliers = suppliers;
    this.lines = lines;
  }

  private Invoice load(InvoiceId id) {
    return repository.findById(id).orElseThrow(InvoiceNotFoundApplicationException::new);
  }

  public InvoiceResponse find(UUID actor, InvoiceId id) {
    return work.execute(
        () -> {
          var value = load(id);
          references.reservation(actor, value.reservationId(), false);
          return InvoiceResponse.from(value);
        });
  }

  public List<InvoiceResponse> list(UUID actor) {
    return work.execute(
        () -> {
          access.requireActive(actor);
          var values =
              access.isAdministrator(actor)
                  ? repository.findAll()
                  : repository.findByParticipant(actor);
          return values.stream().map(InvoiceResponse::from).toList();
        });
  }

  public InvoiceResponse register(UUID actor, RegisterInvoiceCommand command) {
    return work.execute(
        () -> {
          var reservation = references.reservation(actor, command.reservationId(), true);
          WorkflowReferences.require(
              reservation.status() == ReservationStatus.CONFIRMED, "Reservation must be confirmed");
          var rental =
              rentals
                  .findById(new RentalId(reservation.id().value()))
                  .orElseThrow(
                      () ->
                          new com.toolrental.application.common.exception
                              .ReferenceNotFoundApplicationException("Rental"));
          WorkflowReferences.require(
              !"CANCELLED".equals(rental.rentalStatus()), "Rental is cancelled");
          var customer =
              customers
                  .findById(reservation.customerId())
                  .orElseThrow(
                      () ->
                          new com.toolrental.application.common.exception
                              .ReferenceNotFoundApplicationException("Customer"));
          var supplier =
              suppliers
                  .findById(reservation.supplierId())
                  .orElseThrow(
                      () ->
                          new com.toolrental.application.common.exception
                              .ReferenceNotFoundApplicationException("Supplier"));
          WorkflowReferences.require(!command.lines().isEmpty(), "Invoice requires lines");
          var id = InvoiceId.generate();
          if (repository.findById(id).isPresent())
            throw new BusinessRuleException("Invoice already exists");
          var value =
              Invoice.register(
                  id,
                  command.reservationId(),
                  command.number(),
                  Instant.now(clock),
                  "ISSUED",
                  supplier.businessName(),
                  supplier.documentType() + " " + supplier.documentNumber(),
                  customer.billingName(),
                  customer.documentType() + " " + customer.documentNumber(),
                  clock);
          var saved = repository.save(value);
          int lineNumber = 1;
          for (var line : command.lines()) {
            lines.save(
                InvoiceItem.register(
                    new InvoiceItemId(value.id(), lineNumber++),
                    line.description(),
                    line.quantity(),
                    line.unitPrice(),
                    clock));
          }
          return InvoiceResponse.from(saved);
        });
  }

  public InvoiceResponse transition(UUID actor, InvoiceId id, String next) {
    return work.execute(
        () -> {
          var value = load(id);
          references.reservation(actor, value.reservationId(), true);
          return InvoiceResponse.from(repository.save(value.transition(next, clock)));
        });
  }
}
