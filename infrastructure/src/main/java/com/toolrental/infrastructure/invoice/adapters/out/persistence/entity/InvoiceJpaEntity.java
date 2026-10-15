package com.toolrental.infrastructure.invoice.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invoices")
public class InvoiceJpaEntity {
  @Id
  @Column(name = "invoice_id")
  private UUID id;

  @Column(name = "reservation_id", nullable = false)
  private UUID reservationId;

  @Column(name = "number", nullable = false, length = 80)
  private String number;

  @Column(name = "issued_at", nullable = false)
  private Instant issuedAt;

  @Column(name = "invoice_status", nullable = false, length = 20)
  private String invoiceStatus;

  @Column(name = "issued_supplier_name", nullable = false, length = 200)
  private String issuedSupplierName;

  @Column(name = "issued_supplier_document", nullable = false, length = 120)
  private String issuedSupplierDocument;

  @Column(name = "issued_customer_name", nullable = false, length = 200)
  private String issuedCustomerName;

  @Column(name = "issued_customer_document", nullable = false, length = 120)
  private String issuedCustomerDocument;

  protected InvoiceJpaEntity() {}

  public InvoiceJpaEntity(
      UUID id,
      UUID reservationId,
      String number,
      Instant issuedAt,
      String invoiceStatus,
      String issuedSupplierName,
      String issuedSupplierDocument,
      String issuedCustomerName,
      String issuedCustomerDocument) {
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

  public UUID getId() {
    return id;
  }

  public UUID getReservationId() {
    return reservationId;
  }

  public String getNumber() {
    return number;
  }

  public Instant getIssuedAt() {
    return issuedAt;
  }

  public String getInvoiceStatus() {
    return invoiceStatus;
  }

  public String getIssuedSupplierName() {
    return issuedSupplierName;
  }

  public String getIssuedSupplierDocument() {
    return issuedSupplierDocument;
  }

  public String getIssuedCustomerName() {
    return issuedCustomerName;
  }

  public String getIssuedCustomerDocument() {
    return issuedCustomerDocument;
  }
}
