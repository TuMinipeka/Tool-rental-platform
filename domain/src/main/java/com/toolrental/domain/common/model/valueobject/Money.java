package com.toolrental.domain.common.model.valueobject;

import com.toolrental.domain.common.exception.BusinessRuleException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) {
  public Money {
    Objects.requireNonNull(amount, "amount");
    Objects.requireNonNull(currency, "currency");
    if (amount.signum() < 0) throw new BusinessRuleException("Money amount cannot be negative");
    amount = amount.stripTrailingZeros();
  }

  public Money multiply(long quantity) {
    if (quantity < 0) throw new BusinessRuleException("Quantity cannot be negative");
    return new Money(amount.multiply(BigDecimal.valueOf(quantity)), currency);
  }

  public Money multiply(BigDecimal rate) {
    if (rate == null || rate.signum() < 0)
      throw new BusinessRuleException("Rate cannot be negative");
    return new Money(amount.multiply(rate).setScale(4, RoundingMode.HALF_UP), currency);
  }

  public Money subtract(Money other) {
    if (!currency.equals(other.currency))
      throw new BusinessRuleException("Cannot subtract money with different currencies");
    if (amount.compareTo(other.amount) < 0)
      throw new BusinessRuleException("Money result cannot be negative");
    return new Money(amount.subtract(other.amount), currency);
  }

  public Money add(Money other) {
    if (!currency.equals(other.currency))
      throw new BusinessRuleException("Cannot add money with different currencies");
    return new Money(amount.add(other.amount), currency);
  }
}
