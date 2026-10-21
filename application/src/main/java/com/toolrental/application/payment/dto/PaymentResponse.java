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
    Instant confirmedAt,
    String paymentMethod,
    String paymentStage,
    String cardNetwork,
    String cardLastFour,
    Short cardExpiryMonth,
    Short cardExpiryYear) {
  public static PaymentResponse from(Payment value) {
    return new PaymentResponse(
        value.id().value(),
        value.reservationId().value(),
        value.processor(),
        value.externalReference(),
        value.amount(),
        value.paymentStatus(),
        value.recordedAt(),
        value.confirmedAt(),
        value.paymentMethod(),
        value.paymentStage(),
        value.cardNetwork(),
        value.cardLastFour(),
        value.cardExpiryMonth(),
        value.cardExpiryYear());
  }
}
