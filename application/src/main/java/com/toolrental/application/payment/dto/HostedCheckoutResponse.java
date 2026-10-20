package com.toolrental.application.payment.dto;

import com.toolrental.domain.payment.model.valueobject.HostedCheckout;
import java.time.Instant;
import java.util.UUID;

public record HostedCheckoutResponse(
    UUID paymentId,
    UUID reservationId,
    String paymentStatus,
    String checkoutUrl,
    Instant expiresAt) {
  public static HostedCheckoutResponse from(HostedCheckout value) {
    return new HostedCheckoutResponse(
        value.paymentId(),
        value.reservationId(),
        value.phase(),
        value.checkoutUrl(),
        value.expiresAt());
  }
}
