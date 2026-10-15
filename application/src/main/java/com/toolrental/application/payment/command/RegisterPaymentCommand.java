package com.toolrental.application.payment.command;

import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import java.math.BigDecimal;

public record RegisterPaymentCommand(
    ReservationId reservationId, String processor, String externalReference, BigDecimal amount) {}
