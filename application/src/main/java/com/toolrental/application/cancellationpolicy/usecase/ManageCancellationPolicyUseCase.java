package com.toolrental.application.cancellationpolicy.usecase;

import com.toolrental.application.cancellationpolicy.dto.CancellationPolicyResponse;
import com.toolrental.domain.cancellationpolicy.model.aggregate.CancellationPolicy;
import com.toolrental.domain.cancellationpolicy.model.valueobject.CancellationPolicyId;
import com.toolrental.domain.cancellationpolicy.port.repository.CancellationPolicyRepository;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ManageCancellationPolicyUseCase {
  private final CancellationPolicyRepository policies;
  private final AccessPolicy access;
  private final UnitOfWork work;
  private final Clock clock;

  public ManageCancellationPolicyUseCase(
      CancellationPolicyRepository policies, AccessPolicy access, UnitOfWork work, Clock clock) {
    this.policies = policies;
    this.access = access;
    this.work = work;
    this.clock = clock;
  }

  public CancellationPolicyResponse configure(
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

  public List<CancellationPolicyResponse> list(UUID actor) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          return policies.findAll().stream().map(CancellationPolicyResponse::from).toList();
        });
  }
}
