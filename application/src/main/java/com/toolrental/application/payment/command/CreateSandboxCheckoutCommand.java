package com.toolrental.application.payment.command;

import com.toolrental.domain.reservation.model.valueobject.ReservationId;

public record CreateSandboxCheckoutCommand(
    ReservationId reservationId,
    String paymentMethod,
    String idempotencyKey,
    String cardNetwork,
    String cardLastFour,
    Short cardExpiryMonth,
    Short cardExpiryYear) {}
