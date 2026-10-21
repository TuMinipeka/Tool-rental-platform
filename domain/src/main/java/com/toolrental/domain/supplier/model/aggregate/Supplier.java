package com.toolrental.domain.supplier.model.aggregate;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.supplier.event.SupplierChanged;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class Supplier extends AggregateRoot {
  private final SupplierId id;
  private final String documentType;
  private final String documentNumber;
  private final String businessName;

  private Supplier(SupplierId id, String documentType, String documentNumber, String businessName) {
    Objects.requireNonNull(id);
    Objects.requireNonNull(documentType, "documentType is required");
    if (documentType != null && (documentType.isBlank() || documentType.length() > 30))
      throw new BusinessRuleException("Invalid documentType");
    Objects.requireNonNull(documentNumber, "documentNumber is required");
    if (documentNumber != null && (documentNumber.isBlank() || documentNumber.length() > 80))
      throw new BusinessRuleException("Invalid documentNumber");
    Objects.requireNonNull(businessName, "businessName is required");
    if (businessName != null && (businessName.isBlank() || businessName.length() > 200))
      throw new BusinessRuleException("Invalid businessName");
    this.id = id;
    this.documentType = documentType;
    this.documentNumber = documentNumber;
    this.businessName = businessName;
  }

  public static Supplier restore(
      SupplierId id, String documentType, String documentNumber, String businessName) {
    return new Supplier(id, documentType, documentNumber, businessName);
  }

  public static Supplier register(
      SupplierId id, String documentType, String documentNumber, String businessName, Clock clock) {
    var result = new Supplier(id, documentType, documentNumber, businessName);
    result.recordEvent(new SupplierChanged(id, Instant.now(clock)));
    return result;
  }

  public SupplierId id() {
    return id;
  }

  public String documentType() {
    return documentType;
  }

  public String documentNumber() {
    return documentNumber;
  }

  public String businessName() {
    return businessName;
  }

  public Supplier update(
      String documentType, String documentNumber, String businessName, Clock clock) {
    var changed = new Supplier(id, documentType, documentNumber, businessName);
    changed.recordEvent(new SupplierChanged(id, Instant.now(clock)));
    return changed;
  }
}
