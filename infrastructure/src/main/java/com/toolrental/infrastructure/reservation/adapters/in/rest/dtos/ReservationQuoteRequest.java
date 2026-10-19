package com.toolrental.infrastructure.reservation.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ReservationQuoteRequest(
    @NotNull LocalDate startDate,
    @NotNull LocalDate returnDate,
    @NotEmpty List<UUID> equipmentUnitIds) {
  public ReservationQuoteRequest {
    equipmentUnitIds = equipmentUnitIds == null ? null : List.copyOf(equipmentUnitIds);
  }
}
