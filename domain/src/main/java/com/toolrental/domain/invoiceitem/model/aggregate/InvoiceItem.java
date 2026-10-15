package com.toolrental.domain.invoiceitem.model.aggregate;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.invoiceitem.event.InvoiceItemChanged;
import com.toolrental.domain.invoiceitem.model.valueobject.InvoiceItemId;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class InvoiceItem extends AggregateRoot {
  private final InvoiceItemId id;
  private final String issuedDescription;
  private final BigDecimal quantity;
  private final BigDecimal issuedUnitPrice;

  private InvoiceItem(
      InvoiceItemId id, String issuedDescription, BigDecimal quantity, BigDecimal issuedUnitPrice) {
    Objects.requireNonNull(id);
    Objects.requireNonNull(issuedDescription, "issuedDescription is required");
    if (issuedDescription != null
        && (issuedDescription.isBlank() || issuedDescription.length() > 500))
      throw new BusinessRuleException("Invalid issuedDescription");
    Objects.requireNonNull(quantity, "quantity is required");
    if (quantity != null
        && (quantity.signum() <= 0 || quantity.scale() > 4 || quantity.precision() > 19))
      throw new BusinessRuleException("Invalid quantity");
    Objects.requireNonNull(issuedUnitPrice, "issuedUnitPrice is required");
    if (issuedUnitPrice != null
        && (issuedUnitPrice.signum() < 0
            || issuedUnitPrice.scale() > 4
            || issuedUnitPrice.precision() > 19))
      throw new BusinessRuleException("Invalid issuedUnitPrice");
    if (id.lineNumber() <= 0) throw new BusinessRuleException("Line number must be positive");
    this.id = id;
    this.issuedDescription = issuedDescription;
    this.quantity = quantity;
    this.issuedUnitPrice = issuedUnitPrice;
  }

  public static InvoiceItem restore(
      InvoiceItemId id, String issuedDescription, BigDecimal quantity, BigDecimal issuedUnitPrice) {
    return new InvoiceItem(id, issuedDescription, quantity, issuedUnitPrice);
  }

  public static InvoiceItem register(
      InvoiceItemId id,
      String issuedDescription,
      BigDecimal quantity,
      BigDecimal issuedUnitPrice,
      Clock clock) {
    var result = new InvoiceItem(id, issuedDescription, quantity, issuedUnitPrice);
    result.recordEvent(new InvoiceItemChanged(id, Instant.now(clock)));
    return result;
  }

  public InvoiceItemId id() {
    return id;
  }

  public String issuedDescription() {
    return issuedDescription;
  }

  public BigDecimal quantity() {
    return quantity;
  }

  public BigDecimal issuedUnitPrice() {
    return issuedUnitPrice;
  }
}
