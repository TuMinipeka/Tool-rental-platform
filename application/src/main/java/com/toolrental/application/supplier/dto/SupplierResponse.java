package com.toolrental.application.supplier.dto;

import com.toolrental.domain.supplier.model.aggregate.Supplier;
import java.util.UUID;

public record SupplierResponse(
    UUID userId, String documentType, String documentNumber, String businessName) {
  public static SupplierResponse from(Supplier value) {
    return new SupplierResponse(
        value.id().value(), value.documentType(), value.documentNumber(), value.businessName());
  }
}
