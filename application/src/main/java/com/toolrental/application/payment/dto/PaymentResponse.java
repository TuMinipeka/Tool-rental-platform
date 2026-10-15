package com.toolrental.application.payment.dto;

import com.toolrental.domain.payment.model.aggregate.Payment;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
    UUID paymentId,
    UUID reservationId,
    String processor,
    String externalReference,
    BigDecimal amount,
    String paymentStatus,
    Instant recordedAt,
    Instant confirmedAt) {
  public static PaymentResponse from(Payment value) {
    return new PaymentResponse(
        value.id().value(),
        value.reservationId().value(),
        value.processor(),
        value.externalReference(),
        value.amount(),
        value.paymentStatus(),
        value.recordedAt(),
        value.confirmedAt());
  }
}
