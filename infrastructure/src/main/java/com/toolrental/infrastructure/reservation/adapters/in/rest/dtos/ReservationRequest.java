package com.toolrental.infrastructure.reservation.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ReservationRequest(
    @NotNull LocalDate startDate,
    @NotNull LocalDate returnDate,
    @NotEmpty List<@NotNull UUID> equipmentUnitIds) {}
