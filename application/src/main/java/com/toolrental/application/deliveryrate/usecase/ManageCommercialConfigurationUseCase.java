package com.toolrental.application.deliveryrate.usecase;

import com.toolrental.application.cancellationpolicy.dto.CancellationPolicyResponse;
import com.toolrental.application.deliveryrate.dto.DeliveryRateResponse;
import com.toolrental.domain.cancellationpolicy.model.aggregate.CancellationPolicy;
import com.toolrental.domain.cancellationpolicy.model.valueobject.CancellationPolicyId;
import com.toolrental.domain.cancellationpolicy.port.repository.CancellationPolicyRepository;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.deliveryrate.model.aggregate.DeliveryRate;
import com.toolrental.domain.deliveryrate.model.valueobject.DeliveryRateId;
import com.toolrental.domain.deliveryrate.port.repository.DeliveryRateRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ManageCommercialConfigurationUseCase {
  private final DeliveryRateRepository rates;
  private final CancellationPolicyRepository policies;
  private final AccessPolicy access;
  private final UnitOfWork work;
  private final Clock clock;

  public ManageCommercialConfigurationUseCase(
      DeliveryRateRepository r,
      CancellationPolicyRepository p,
      AccessPolicy a,
      UnitOfWork w,
      Clock c) {
    rates = r;
    policies = p;
    access = a;
    work = w;
    clock = c;
  }

  public DeliveryRateResponse configureRate(UUID actor, Money price) {
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

  public List<DeliveryRateResponse> rates(UUID actor) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          return rates.findAll().stream().map(DeliveryRateResponse::from).toList();
        });
  }

  public CancellationPolicyResponse configurePolicy(
      UUID actor, int cutoff, BigDecimal late, BigDecimal noShow, int grace) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          int version =
              policies.findAll().stream().mapToInt(CancellationPolicy::version).max().orElse(0) + 1;
          return CancellationPolicyResponse.from(
              policies.save(
                  CancellationPolicy.register(
                      CancellationPolicyId.generate(),
                      version,
                      cutoff,
                      late,
                      noShow,
                      grace,
                      Instant.now(clock))));
        });
  }

  public List<CancellationPolicyResponse> policies(UUID actor) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          return policies.findAll().stream().map(CancellationPolicyResponse::from).toList();
        });
  }
}
