package com.toolrental.domain.rental.model.aggregate;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.rental.event.RentalChanged;
import com.toolrental.domain.rental.model.valueobject.RentalId;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public final class Rental extends AggregateRoot {
  private final RentalId id;
  private final Instant startedAt;
  private final String rentalStatus;

  private Rental(RentalId id, Instant startedAt, String rentalStatus) {
    Objects.requireNonNull(id);
    Objects.requireNonNull(startedAt, "startedAt is required");
    Objects.requireNonNull(rentalStatus, "rentalStatus is required");
    if (rentalStatus != null && (rentalStatus.isBlank() || rentalStatus.length() > 20))
      throw new BusinessRuleException("Invalid rentalStatus");
    if (!Set.of("ACTIVE", "COMPLETED", "CANCELLED").contains(rentalStatus))
      throw new BusinessRuleException("Invalid rentalStatus");
    this.id = id;
    this.startedAt = startedAt;
    this.rentalStatus = rentalStatus;
  }

  public static Rental restore(RentalId id, Instant startedAt, String rentalStatus) {
    return new Rental(id, startedAt, rentalStatus);
  }

  public static Rental register(RentalId id, Instant startedAt, String rentalStatus, Clock clock) {
    var result = new Rental(id, startedAt, rentalStatus);
    result.recordEvent(new RentalChanged(id, Instant.now(clock)));
    return result;
  }

  public RentalId id() {
    return id;
  }

  public Instant startedAt() {
    return startedAt;
  }

  public String rentalStatus() {
    return rentalStatus;
  }

  public Rental transition(String next, Clock clock) {
    if (!(("ACTIVE".equals(rentalStatus) && Set.of("COMPLETED", "CANCELLED").contains(next))))
      throw new BusinessRuleException("Invalid Rental transition");
    var result = new Rental(id, startedAt, next);
    result.recordEvent(new RentalChanged(id, Instant.now(clock)));
    return result;
  }
}
