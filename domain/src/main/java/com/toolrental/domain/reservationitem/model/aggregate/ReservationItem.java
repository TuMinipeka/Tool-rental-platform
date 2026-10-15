package com.toolrental.domain.reservationitem.model.aggregate;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.reservationitem.event.ReservationItemChanged;
import com.toolrental.domain.reservationitem.model.valueobject.ReservationItemId;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class ReservationItem extends AggregateRoot {
  private final ReservationItemId id;
  private final BigDecimal agreedDailyRate;

  private ReservationItem(ReservationItemId id, BigDecimal agreedDailyRate) {
    Objects.requireNonNull(id);
    Objects.requireNonNull(agreedDailyRate, "agreedDailyRate is required");
    if (agreedDailyRate != null
        && (agreedDailyRate.signum() < 0
            || agreedDailyRate.scale() > 4
            || agreedDailyRate.precision() > 19))
      throw new BusinessRuleException("Invalid agreedDailyRate");
    this.id = id;
    this.agreedDailyRate = agreedDailyRate;
  }

  public static ReservationItem restore(ReservationItemId id, BigDecimal agreedDailyRate) {
    return new ReservationItem(id, agreedDailyRate);
  }

  public static ReservationItem register(
      ReservationItemId id, BigDecimal agreedDailyRate, Clock clock) {
    var result = new ReservationItem(id, agreedDailyRate);
    result.recordEvent(new ReservationItemChanged(id, Instant.now(clock)));
    return result;
  }

  public ReservationItemId id() {
    return id;
  }

  public BigDecimal agreedDailyRate() {
    return agreedDailyRate;
  }
}
