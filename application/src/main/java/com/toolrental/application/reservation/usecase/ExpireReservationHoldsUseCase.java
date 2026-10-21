package com.toolrental.application.reservation.usecase;

import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import com.toolrental.domain.reservation.port.repository.AtomicReservationManagement;
import com.toolrental.domain.reservation.port.repository.ReservationRepository;
import java.time.Clock;
import java.time.Instant;

public final class ExpireReservationHoldsUseCase {
  private final ReservationRepository reservations;
  private final AtomicReservationManagement management;
  private final Clock clock;

  public ExpireReservationHoldsUseCase(
      ReservationRepository reservations, AtomicReservationManagement management, Clock clock) {
    this.reservations = reservations;
    this.management = management;
    this.clock = clock;
  }

  public int execute() {
    var expired = reservations.findExpiredPaymentHolds(Instant.now(clock));
    var count = 0;
    for (var reservation : expired) {
      management.execute(
          reservation.id(),
          context -> {
            if (context.reservation().status() != ReservationStatus.PENDING_PAYMENT) {
              return context.reservation();
            }
            return context.reservation().expire(clock);
          });
      count++;
    }
    return count;
  }
}
