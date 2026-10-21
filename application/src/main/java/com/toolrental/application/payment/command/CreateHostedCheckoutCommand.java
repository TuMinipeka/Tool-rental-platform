package com.toolrental.application.payment.command;

import com.toolrental.domain.reservation.model.valueobject.ReservationId;

public record CreateHostedCheckoutCommand(
    ReservationId reservationId,
    String paymentMethod,
    String idempotencyKey,
    String cardNetwork,
    String cardLastFour,
    Short cardExpiryMonth,
    Short cardExpiryYear) {}
