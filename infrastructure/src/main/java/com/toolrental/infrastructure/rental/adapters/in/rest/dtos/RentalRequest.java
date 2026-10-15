package com.toolrental.infrastructure.rental.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RentalRequest(@NotNull UUID reservationId) {}
