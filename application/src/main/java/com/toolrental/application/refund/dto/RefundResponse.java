package com.toolrental.application.refund.dto;

import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.payment.model.valueobject.PaymentId;
import com.toolrental.domain.refund.model.aggregate.Refund;
import com.toolrental.domain.refund.model.valueobject.RefundId;
import java.time.Instant;

public record RefundResponse(
    RefundId id, PaymentId paymentId, Money amount, String status, Instant recordedAt) {
  public static RefundResponse from(Refund value) {
    return new RefundResponse(
        value.id(), value.paymentId(), value.amount(), value.status(), value.recordedAt());
  }
}
