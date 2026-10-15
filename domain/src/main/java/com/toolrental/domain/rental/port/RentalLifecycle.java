package com.toolrental.domain.rental.port;

import com.toolrental.domain.reservation.model.valueobject.ReservationId;

public interface RentalLifecycle {
  boolean allUnitsReturned(ReservationId id);

  boolean hasDelivery(ReservationId id);
}
