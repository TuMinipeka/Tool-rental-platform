package com.toolrental.domain.reservation.model.valueobject;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.valueobject.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record ReservationQuote(
    ReservationPeriod period, Money rentalSubtotal, Money firstPayment, Money remainingPayment) {
  public ReservationQuote {
    Objects.requireNonNull(period, "period is required");
    Objects.requireNonNull(rentalSubtotal, "rentalSubtotal is required");
    Objects.requireNonNull(firstPayment, "firstPayment is required");
    Objects.requireNonNull(remainingPayment, "remainingPayment is required");
  }

  public static ReservationQuote calculate(ReservationPeriod period, List<Money> dailyRates) {
    var rates = List.copyOf(dailyRates);
    if (rates.isEmpty()) throw new BusinessRuleException("A quote requires at least one unit");
    var currency = rates.getFirst().currency();
    var subtotal = new Money(BigDecimal.ZERO, currency);
    for (var rate : rates) {
      if (!currency.equals(rate.currency()))
        throw new BusinessRuleException("All quoted units must use one currency");
      subtotal = subtotal.add(rate.multiply(period.billableDays()));
    }
    var firstAmount = subtotal.amount().multiply(new BigDecimal("0.50"));
    var remainingAmount = subtotal.amount().subtract(firstAmount);
    return new ReservationQuote(
        period, subtotal, new Money(firstAmount, currency), new Money(remainingAmount, currency));
  }
}
