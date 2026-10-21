package com.toolrental.infrastructure.invoiceitem.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class InvoiceItemJpaId implements Serializable {
  @Column(name = "invoice_id")
  private UUID invoiceId;

  @Column(name = "line_number")
  private Integer lineNumber;

  protected InvoiceItemJpaId() {}

  public InvoiceItemJpaId(UUID invoiceId, Integer lineNumber) {
    this.invoiceId = invoiceId;
    this.lineNumber = lineNumber;
  }

  public UUID getInvoiceId() {
    return invoiceId;
  }

  public Integer getLineNumber() {
    return lineNumber;
  }

  public boolean equals(Object other) {
    return other instanceof InvoiceItemJpaId key
        && Objects.equals(invoiceId, key.invoiceId)
        && Objects.equals(lineNumber, key.lineNumber);
  }

  public int hashCode() {
    return Objects.hash(invoiceId, lineNumber);
  }
}
