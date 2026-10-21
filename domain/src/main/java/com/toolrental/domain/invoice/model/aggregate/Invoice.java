package com.toolrental.domain.invoice.model.aggregate;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.invoice.event.InvoiceChanged;
import com.toolrental.domain.invoice.model.valueobject.InvoiceId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public final class Invoice extends AggregateRoot {
  private final InvoiceId id;
  private final ReservationId reservationId;
  private final String number;
  private final Instant issuedAt;
  private final String invoiceStatus;
  private final String issuedSupplierName;
  private final String issuedSupplierDocument;
  private final String issuedCustomerName;
  private final String issuedCustomerDocument;

  private Invoice(
      InvoiceId id,
      ReservationId reservationId,
      String number,
      Instant issuedAt,
      String invoiceStatus,
      String issuedSupplierName,
      String issuedSupplierDocument,
      String issuedCustomerName,
      String issuedCustomerDocument) {
    Objects.requireNonNull(id);
    Objects.requireNonNull(reservationId, "reservationId is required");
    Objects.requireNonNull(number, "number is required");
    if (number != null && (number.isBlank() || number.length() > 80))
      throw new BusinessRuleException("Invalid number");
    Objects.requireNonNull(issuedAt, "issuedAt is required");
    Objects.requireNonNull(invoiceStatus, "invoiceStatus is required");
    if (invoiceStatus != null && (invoiceStatus.isBlank() || invoiceStatus.length() > 20))
      throw new BusinessRuleException("Invalid invoiceStatus");
    if (!Set.of("ISSUED", "VOIDED", "PAID").contains(invoiceStatus))
      throw new BusinessRuleException("Invalid invoiceStatus");
    Objects.requireNonNull(issuedSupplierName, "issuedSupplierName is required");
    if (issuedSupplierName != null
        && (issuedSupplierName.isBlank() || issuedSupplierName.length() > 200))
      throw new BusinessRuleException("Invalid issuedSupplierName");
    Objects.requireNonNull(issuedSupplierDocument, "issuedSupplierDocument is required");
    if (issuedSupplierDocument != null
        && (issuedSupplierDocument.isBlank() || issuedSupplierDocument.length() > 120))
      throw new BusinessRuleException("Invalid issuedSupplierDocument");
    Objects.requireNonNull(issuedCustomerName, "issuedCustomerName is required");
    if (issuedCustomerName != null
        && (issuedCustomerName.isBlank() || issuedCustomerName.length() > 200))
      throw new BusinessRuleException("Invalid issuedCustomerName");
    Objects.requireNonNull(issuedCustomerDocument, "issuedCustomerDocument is required");
    if (issuedCustomerDocument != null
        && (issuedCustomerDocument.isBlank() || issuedCustomerDocument.length() > 120))
      throw new BusinessRuleException("Invalid issuedCustomerDocument");
    this.id = id;
    this.reservationId = reservationId;
    this.number = number;
    this.issuedAt = issuedAt;
    this.invoiceStatus = invoiceStatus;
    this.issuedSupplierName = issuedSupplierName;
    this.issuedSupplierDocument = issuedSupplierDocument;
    this.issuedCustomerName = issuedCustomerName;
    this.issuedCustomerDocument = issuedCustomerDocument;
  }

  public static Invoice restore(
      InvoiceId id,
      ReservationId reservationId,
      String number,
      Instant issuedAt,
      String invoiceStatus,
      String issuedSupplierName,
      String issuedSupplierDocument,
      String issuedCustomerName,
      String issuedCustomerDocument) {
    return new Invoice(
        id,
        reservationId,
        number,
        issuedAt,
        invoiceStatus,
        issuedSupplierName,
        issuedSupplierDocument,
        issuedCustomerName,
        issuedCustomerDocument);
  }

  public static Invoice register(
      InvoiceId id,
      ReservationId reservationId,
      String number,
      Instant issuedAt,
      String invoiceStatus,
      String issuedSupplierName,
      String issuedSupplierDocument,
      String issuedCustomerName,
      String issuedCustomerDocument,
      Clock clock) {
    var result =
        new Invoice(
            id,
            reservationId,
            number,
            issuedAt,
            invoiceStatus,
            issuedSupplierName,
            issuedSupplierDocument,
            issuedCustomerName,
            issuedCustomerDocument);
    result.recordEvent(new InvoiceChanged(id, Instant.now(clock)));
    return result;
  }

  public InvoiceId id() {
    return id;
  }

  public ReservationId reservationId() {
    return reservationId;
  }

  public String number() {
    return number;
  }

  public Instant issuedAt() {
    return issuedAt;
  }

  public String invoiceStatus() {
    return invoiceStatus;
  }

  public String issuedSupplierName() {
    return issuedSupplierName;
  }

  public String issuedSupplierDocument() {
    return issuedSupplierDocument;
  }

  public String issuedCustomerName() {
    return issuedCustomerName;
  }

  public String issuedCustomerDocument() {
    return issuedCustomerDocument;
  }

  public Invoice transition(String next, Clock clock) {
    if (!(("ISSUED".equals(invoiceStatus) && Set.of("PAID", "VOIDED").contains(next))))
      throw new BusinessRuleException("Invalid Invoice transition");
    var result =
        new Invoice(
            id,
            reservationId,
            number,
            issuedAt,
            next,
            issuedSupplierName,
            issuedSupplierDocument,
            issuedCustomerName,
            issuedCustomerDocument);
    result.recordEvent(new InvoiceChanged(id, Instant.now(clock)));
    return result;
  }
}
