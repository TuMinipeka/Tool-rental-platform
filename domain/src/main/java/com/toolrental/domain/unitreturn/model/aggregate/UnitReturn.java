package com.toolrental.domain.unitreturn.model.aggregate;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.unitreturn.event.UnitReturnChanged;
import com.toolrental.domain.unitreturn.model.valueobject.UnitReturnId;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class UnitReturn extends AggregateRoot {
  private final UnitReturnId id;
  private final Instant receivedAt;
  private final String inboundCondition;
  private final String notes;

  private UnitReturn(UnitReturnId id, Instant receivedAt, String inboundCondition, String notes) {
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
  }

  public static UnitReturn restore(
      UnitReturnId id, Instant receivedAt, String inboundCondition, String notes) {
    return new UnitReturn(id, receivedAt, inboundCondition, notes);
  }

  public static UnitReturn register(
      UnitReturnId id, Instant receivedAt, String inboundCondition, String notes, Clock clock) {
    var result = new UnitReturn(id, receivedAt, inboundCondition, notes);
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
}
