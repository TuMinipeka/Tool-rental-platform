package com.toolrental.infrastructure.reservationextension.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationExtensionRequest(@NotNull LocalDate requestedReturnDate) {}
