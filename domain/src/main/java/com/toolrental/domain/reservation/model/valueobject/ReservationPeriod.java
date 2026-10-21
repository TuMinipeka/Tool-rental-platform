package com.toolrental.domain.reservation.model.valueobject;

import com.toolrental.domain.common.exception.BusinessRuleException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/** Both dates are inclusive for billing and availability. */
public record ReservationPeriod(LocalDate startDate, LocalDate returnDate) {
  public ReservationPeriod {
    Objects.requireNonNull(startDate, "startDate");
    Objects.requireNonNull(returnDate, "returnDate");
    if (returnDate.isBefore(startDate))
      throw new BusinessRuleException("Return date cannot be before start date");
  }

  public long billableDays() {
    return ChronoUnit.DAYS.between(startDate, returnDate) + 1;
  }

  public boolean overlaps(ReservationPeriod other) {
    return !returnDate.isBefore(other.startDate) && !other.returnDate.isBefore(startDate);
  }
}
