package com.toolrental.application.payment.dto;

import com.toolrental.domain.payment.model.aggregate.Payment;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CustomerPaymentResponse(
    UUID paymentId, Instant paymentDate, BigDecimal amount, String paymentMethod, String status) {
  public static CustomerPaymentResponse from(Payment payment) {
    return new CustomerPaymentResponse(
        payment.id().value(),
        payment.confirmedAt() == null ? payment.recordedAt() : payment.confirmedAt(),
        payment.amount(),
        "ADMINISTRATIVE".equals(payment.paymentMethod()) ? null : payment.paymentMethod(),
        payment.paymentStatus());
  }
}
