package com.toolrental.application.customer.dto;

import com.toolrental.domain.customer.model.aggregate.Customer;
import java.util.UUID;

public record CustomerResponse(
    UUID userId,
    String personType,
    String documentType,
    String documentNumber,
    String billingName) {
  public static CustomerResponse from(Customer value) {
    return new CustomerResponse(
        value.id().value(),
        value.personType(),
        value.documentType(),
        value.documentNumber(),
        value.billingName());
  }
}
