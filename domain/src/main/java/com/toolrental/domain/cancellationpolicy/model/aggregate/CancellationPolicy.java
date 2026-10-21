package com.toolrental.domain.cancellationpolicy.model.aggregate;

import com.toolrental.domain.cancellationpolicy.event.CancellationPolicyChanged;
import com.toolrental.domain.cancellationpolicy.model.valueobject.CancellationPolicyId;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class CancellationPolicy extends AggregateRoot {
  private final CancellationPolicyId id;
  private final int version;
  private final int cutoffHours;
  private final BigDecimal lateCancellationRate;
  private final BigDecimal noShowRate;
  private final int noShowGraceMinutes;
  private final boolean active;
  private final Instant effectiveAt;

  private CancellationPolicy(
      CancellationPolicyId id,
      int version,
      int cutoffHours,
      BigDecimal lateCancellationRate,
      BigDecimal noShowRate,
      int noShowGraceMinutes,
      boolean active,
      Instant effectiveAt) {
    this.id = Objects.requireNonNull(id);
    if (version < 1 || cutoffHours < 0 || noShowGraceMinutes < 0)
      throw new BusinessRuleException("Invalid cancellation policy values");
    validateRate(lateCancellationRate);
    validateRate(noShowRate);
    this.version = version;
    this.cutoffHours = cutoffHours;
    this.lateCancellationRate = lateCancellationRate.stripTrailingZeros();
    this.noShowRate = noShowRate.stripTrailingZeros();
    this.noShowGraceMinutes = noShowGraceMinutes;
    this.active = active;
    this.effectiveAt = Objects.requireNonNull(effectiveAt);
  }

  private static void validateRate(BigDecimal value) {
    if (value == null || value.signum() < 0 || value.compareTo(BigDecimal.ONE) > 0)
      throw new BusinessRuleException("Invalid cancellation rate");
  }

  public static CancellationPolicy restore(
      CancellationPolicyId id,
      int version,
      int cutoffHours,
      BigDecimal lateRate,
      BigDecimal noShowRate,
      int graceMinutes,
      boolean active,
      Instant effectiveAt) {
    return new CancellationPolicy(
        id, version, cutoffHours, lateRate, noShowRate, graceMinutes, active, effectiveAt);
  }

  public static CancellationPolicy register(
      CancellationPolicyId id,
      int version,
      int cutoffHours,
      BigDecimal lateRate,
      BigDecimal noShowRate,
      int graceMinutes,
      Instant now) {
    var result =
        new CancellationPolicy(
            id, version, cutoffHours, lateRate, noShowRate, graceMinutes, true, now);
    result.recordEvent(new CancellationPolicyChanged(id, now));
    return result;
  }

  public boolean isFreeCancellation(Instant scheduledAt, Instant requestedAt) {
    return !requestedAt.isAfter(scheduledAt.minus(Duration.ofHours(cutoffHours)));
  }

  public Instant noShowEligibleAt(Instant scheduledAt) {
    return scheduledAt.plus(Duration.ofMinutes(noShowGraceMinutes));
  }

  public CancellationPolicyId id() {
    return id;
  }

  public int version() {
    return version;
  }

  public int cutoffHours() {
    return cutoffHours;
  }

  public BigDecimal lateCancellationRate() {
    return lateCancellationRate;
  }

  public BigDecimal noShowRate() {
    return noShowRate;
  }

  public int noShowGraceMinutes() {
    return noShowGraceMinutes;
  }

  public boolean active() {
    return active;
  }

  public Instant effectiveAt() {
    return effectiveAt;
  }
}
