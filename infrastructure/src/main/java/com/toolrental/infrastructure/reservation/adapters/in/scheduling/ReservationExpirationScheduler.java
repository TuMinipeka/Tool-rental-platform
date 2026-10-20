package com.toolrental.infrastructure.reservation.adapters.in.scheduling;

import com.toolrental.application.reservation.usecase.ExpireReservationHoldsUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public final class ReservationExpirationScheduler {
  private final ExpireReservationHoldsUseCase expiration;

  public ReservationExpirationScheduler(ExpireReservationHoldsUseCase expiration) {
    this.expiration = expiration;
  }

  @Scheduled(fixedDelayString = "${reservation.expiration-scan-delay:PT1M}")
  public void expirePaymentHolds() {
    expiration.execute();
  }
}
