package com.toolrental.domain.deliveryrate.model.aggregate;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.deliveryrate.event.DeliveryRateChanged;
import com.toolrental.domain.deliveryrate.model.valueobject.DeliveryRateId;
import java.time.Instant;
import java.util.Currency;
import java.util.Objects;

public final class DeliveryRate extends AggregateRoot {
  private final DeliveryRateId id;
  private final int version;
  private final Money price;
  private final boolean active;
  private final Instant effectiveAt;

  private DeliveryRate(
      DeliveryRateId id, int version, Money price, boolean active, Instant effectiveAt) {
    this.id = Objects.requireNonNull(id);
    if (version < 1) throw new BusinessRuleException("Invalid delivery rate version");
    this.price = Objects.requireNonNull(price);
    if (!Currency.getInstance("COP").equals(price.currency()))
      throw new BusinessRuleException("Delivery rate must use COP");
    this.version = version;
    this.active = active;
    this.effectiveAt = Objects.requireNonNull(effectiveAt);
  }

  public static DeliveryRate restore(
      DeliveryRateId id, int version, Money price, boolean active, Instant effectiveAt) {
    return new DeliveryRate(id, version, price, active, effectiveAt);
  }

  public static DeliveryRate register(DeliveryRateId id, int version, Money price, Instant now) {
    var result = new DeliveryRate(id, version, price, true, now);
    result.recordEvent(new DeliveryRateChanged(id, now));
    return result;
  }

  public DeliveryRateId id() {
    return id;
  }

  public int version() {
    return version;
  }

  public Money price() {
    return price;
  }

  public boolean active() {
    return active;
  }

  public Instant effectiveAt() {
    return effectiveAt;
  }
}
