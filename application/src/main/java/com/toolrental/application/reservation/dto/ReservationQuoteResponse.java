package com.toolrental.application.reservation.dto;

import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.reservation.model.valueobject.FulfillmentMethod;
import com.toolrental.domain.reservation.model.valueobject.ReservationQuote;
import java.math.BigDecimal;

public record ReservationQuoteResponse(
    long billableDays,
    Money rentalSubtotal,
    Money deliveryFee,
    Money total,
    Money firstPayment,
    Money remainingPayment,
    FulfillmentMethod fulfillmentMethod,
    int cancellationPolicyVersion,
    String taxCalculationStatus) {
  public static ReservationQuoteResponse from(ReservationQuote quote) {
    return new ReservationQuoteResponse(
        quote.period().billableDays(),
        quote.rentalSubtotal(),
        new Money(BigDecimal.ZERO, quote.rentalSubtotal().currency()),
        quote.rentalSubtotal(),
        quote.firstPayment(),
        quote.remainingPayment(),
        FulfillmentMethod.CUSTOMER_PICKUP,
        1,
        "PENDING_LEGAL_VALIDATION");
  }

  public static ReservationQuoteResponse from(
      ReservationQuote quote, Money deliveryFee, FulfillmentMethod method, int policyVersion) {
    return new ReservationQuoteResponse(
        quote.period().billableDays(),
        quote.rentalSubtotal(),
        deliveryFee,
        quote.rentalSubtotal().add(deliveryFee),
        quote.firstPayment(),
        quote.remainingPayment(),
        method,
        policyVersion,
        "PENDING_LEGAL_VALIDATION");
  }
}
