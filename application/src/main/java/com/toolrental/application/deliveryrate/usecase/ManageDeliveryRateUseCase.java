package com.toolrental.application.deliveryrate.usecase;

import com.toolrental.application.deliveryrate.dto.DeliveryRateResponse;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.deliveryrate.model.aggregate.DeliveryRate;
import com.toolrental.domain.deliveryrate.model.valueobject.DeliveryRateId;
import com.toolrental.domain.deliveryrate.port.repository.DeliveryRateRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ManageDeliveryRateUseCase {
  private final DeliveryRateRepository rates;
  private final AccessPolicy access;
  private final UnitOfWork work;
  private final Clock clock;

  public ManageDeliveryRateUseCase(
      DeliveryRateRepository rates, AccessPolicy access, UnitOfWork work, Clock clock) {
    this.rates = rates;
    this.access = access;
    this.work = work;
    this.clock = clock;
  }

  public DeliveryRateResponse configure(UUID actor, Money price) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          int version =
              rates.findAll().stream().mapToInt(DeliveryRate::version).max().orElse(0) + 1;
          return DeliveryRateResponse.from(
              rates.save(
                  DeliveryRate.register(
                      DeliveryRateId.generate(), version, price, Instant.now(clock))));
        });
  }

  public List<DeliveryRateResponse> list(UUID actor) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          return rates.findAll().stream().map(DeliveryRateResponse::from).toList();
        });
  }
}
