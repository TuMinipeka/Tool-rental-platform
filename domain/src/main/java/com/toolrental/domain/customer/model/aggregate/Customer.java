package com.toolrental.domain.customer.model.aggregate;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.customer.event.CustomerChanged;
import com.toolrental.domain.customer.model.valueobject.CustomerId;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public final class Customer extends AggregateRoot {
  private final CustomerId id;
  private final String personType;
  private final String documentType;
  private final String documentNumber;
  private final String billingName;

  private Customer(
      CustomerId id,
      String personType,
      String documentType,
      String documentNumber,
      String billingName) {
    Objects.requireNonNull(id);
    Objects.requireNonNull(personType, "personType is required");
    if (personType != null && (personType.isBlank() || personType.length() > 20))
      throw new BusinessRuleException("Invalid personType");
    if (!Set.of("PERSON", "COMPANY").contains(personType))
      throw new BusinessRuleException("Invalid personType");
    Objects.requireNonNull(documentType, "documentType is required");
    if (documentType != null && (documentType.isBlank() || documentType.length() > 30))
      throw new BusinessRuleException("Invalid documentType");
    Objects.requireNonNull(documentNumber, "documentNumber is required");
    if (documentNumber != null && (documentNumber.isBlank() || documentNumber.length() > 80))
      throw new BusinessRuleException("Invalid documentNumber");
    Objects.requireNonNull(billingName, "billingName is required");
    if (billingName != null && (billingName.isBlank() || billingName.length() > 200))
      throw new BusinessRuleException("Invalid billingName");
    this.id = id;
    this.personType = personType;
    this.documentType = documentType;
    this.documentNumber = documentNumber;
    this.billingName = billingName;
  }

  public static Customer restore(
      CustomerId id,
      String personType,
      String documentType,
      String documentNumber,
      String billingName) {
    return new Customer(id, personType, documentType, documentNumber, billingName);
  }

  public static Customer register(
      CustomerId id,
      String personType,
      String documentType,
      String documentNumber,
      String billingName,
      Clock clock) {
    var result = new Customer(id, personType, documentType, documentNumber, billingName);
    result.recordEvent(new CustomerChanged(id, Instant.now(clock)));
    return result;
  }

  public CustomerId id() {
    return id;
  }

  public String personType() {
    return personType;
  }

  public String documentType() {
    return documentType;
  }

  public String documentNumber() {
    return documentNumber;
  }

  public String billingName() {
    return billingName;
  }

  public Customer update(
      String personType,
      String documentType,
      String documentNumber,
      String billingName,
      Clock clock) {
    var changed = new Customer(id, personType, documentType, documentNumber, billingName);
    changed.recordEvent(new CustomerChanged(id, Instant.now(clock)));
    return changed;
  }
}
