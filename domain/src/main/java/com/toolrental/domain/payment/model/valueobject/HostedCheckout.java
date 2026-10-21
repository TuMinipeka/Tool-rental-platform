package com.toolrental.domain.payment.model.valueobject;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record HostedCheckout(
    UUID paymentId,
    String processor,
    UUID reservationId,
    BigDecimal amount,
    Instant expiresAt,
    String preferenceId,
    String checkoutUrl,
    String providerPaymentId,
    String phase,
    int attempts,
    Instant leaseUntil) {}
