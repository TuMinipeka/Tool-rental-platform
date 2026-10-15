package com.toolrental.infrastructure.invoiceitem.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
public class InvoiceItemJpaEntity {
  @EmbeddedId private InvoiceItemJpaId id;

  @Column(name = "issued_description", nullable = false, length = 500)
  private String issuedDescription;

  @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
  private BigDecimal quantity;

  @Column(name = "issued_unit_price", nullable = false, precision = 19, scale = 4)
  private BigDecimal issuedUnitPrice;

  protected InvoiceItemJpaEntity() {}

  public InvoiceItemJpaEntity(
      InvoiceItemJpaId id,
      String issuedDescription,
      BigDecimal quantity,
      BigDecimal issuedUnitPrice) {
    this.id = id;
    this.issuedDescription = issuedDescription;
    this.quantity = quantity;
    this.issuedUnitPrice = issuedUnitPrice;
  }

  public InvoiceItemJpaId getId() {
    return id;
  }

  public String getIssuedDescription() {
    return issuedDescription;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public BigDecimal getIssuedUnitPrice() {
    return issuedUnitPrice;
  }
}
