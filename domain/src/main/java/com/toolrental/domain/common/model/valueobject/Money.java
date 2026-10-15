package com.toolrental.domain.common.model.valueobject;

import com.toolrental.domain.common.exception.BusinessRuleException;
import java.math.BigDecimal;
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
    if (quantity < 0) throw new BusinessRuleException("Cantidad negativa");
    return new Money(amount.multiply(BigDecimal.valueOf(quantity)), currency);
  }

  public Money add(Money other) {
    if (!currency.equals(other.currency))
      throw new BusinessRuleException("Cannot add money with different currencies");
    return new Money(amount.add(other.amount), currency);
  }
}
