package com.toolrental.domain.unitreturn.model.aggregate;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.unitreturn.event.UnitReturnChanged;
import com.toolrental.domain.unitreturn.model.valueobject.UnitReturnId;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Currency;
import java.util.Objects;

public final class UnitReturn extends AggregateRoot {
  private final UnitReturnId id;
  private final Instant receivedAt;
  private final String inboundCondition;
  private final String notes;
  private final int lateDays;
  private final Money lateFee;

  private UnitReturn(
      UnitReturnId id,
      Instant receivedAt,
      String inboundCondition,
      String notes,
      int lateDays,
      Money lateFee) {
    Objects.requireNonNull(id);
    Objects.requireNonNull(receivedAt, "receivedAt is required");
    Objects.requireNonNull(inboundCondition, "inboundCondition is required");
    if (inboundCondition != null && (inboundCondition.isBlank() || inboundCondition.length() > 500))
      throw new BusinessRuleException("Invalid inboundCondition");
    if (notes != null && (notes.isBlank() || notes.length() > 2000))
      throw new BusinessRuleException("Invalid notes");
    this.id = id;
    this.receivedAt = receivedAt;
    this.inboundCondition = inboundCondition;
    this.notes = notes;
    if (lateDays < 0) throw new BusinessRuleException("Late days cannot be negative");
    this.lateDays = lateDays;
    this.lateFee = Objects.requireNonNull(lateFee);
  }

  public static UnitReturn restore(
      UnitReturnId id, Instant receivedAt, String inboundCondition, String notes) {
    return new UnitReturn(
        id,
        receivedAt,
        inboundCondition,
        notes,
        0,
        new Money(BigDecimal.ZERO, Currency.getInstance("COP")));
  }

  public static UnitReturn restore(
      UnitReturnId id,
      Instant receivedAt,
      String condition,
      String notes,
      int lateDays,
      Money lateFee) {
    return new UnitReturn(id, receivedAt, condition, notes, lateDays, lateFee);
  }

  public static UnitReturn register(
      UnitReturnId id, Instant receivedAt, String inboundCondition, String notes, Clock clock) {
    var result =
        new UnitReturn(
            id,
            receivedAt,
            inboundCondition,
            notes,
            0,
            new Money(BigDecimal.ZERO, Currency.getInstance("COP")));
    result.recordEvent(new UnitReturnChanged(id, Instant.now(clock)));
    return result;
  }

  public static UnitReturn register(
      UnitReturnId id,
      Instant receivedAt,
      String inboundCondition,
      String notes,
      int lateDays,
      Money lateFee,
      Clock clock) {
    var result = new UnitReturn(id, receivedAt, inboundCondition, notes, lateDays, lateFee);
    result.recordEvent(new UnitReturnChanged(id, Instant.now(clock)));
    return result;
  }

  public UnitReturnId id() {
    return id;
  }

  public Instant receivedAt() {
    return receivedAt;
  }

  public String inboundCondition() {
    return inboundCondition;
  }

  public String notes() {
    return notes;
  }

  public int lateDays() {
    return lateDays;
  }

  public Money lateFee() {
    return lateFee;
  }
}
