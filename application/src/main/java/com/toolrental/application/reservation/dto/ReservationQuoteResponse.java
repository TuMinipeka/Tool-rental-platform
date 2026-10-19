package com.toolrental.application.reservation.dto;

import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.reservation.model.valueobject.ReservationQuote;

public record ReservationQuoteResponse(
    long billableDays,
    Money rentalSubtotal,
    Money firstPayment,
    Money remainingPayment,
    String taxCalculationStatus) {
  public static ReservationQuoteResponse from(ReservationQuote quote) {
    return new ReservationQuoteResponse(
        quote.period().billableDays(),
        quote.rentalSubtotal(),
        quote.firstPayment(),
        quote.remainingPayment(),
        "PENDING_LEGAL_VALIDATION");
  }
}
