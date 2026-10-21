package com.toolrental.application.rental.dto;

import com.toolrental.domain.rental.model.aggregate.Rental;
import java.time.Instant;
import java.util.UUID;

public record RentalResponse(UUID reservationId, Instant startedAt, String rentalStatus) {
  public static RentalResponse from(Rental value) {
    return new RentalResponse(value.id().value(), value.startedAt(), value.rentalStatus());
  }
}
