package com.toolrental.infrastructure.cancellationpolicy.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cancellation_policies")
public class CancellationPolicyJpaEntity {
  @Id
  @Column(name = "cancellation_policy_id")
  private UUID id;

  @Column(name = "policy_version", nullable = false)
  private int version;

  @Column(name = "cutoff_hours", nullable = false)
  private int cutoffHours;

  @Column(name = "late_cancellation_rate", nullable = false, precision = 5, scale = 4)
  private BigDecimal lateRate;

  @Column(name = "no_show_rate", nullable = false, precision = 5, scale = 4)
  private BigDecimal noShowRate;

  @Column(name = "no_show_grace_minutes", nullable = false)
  private int graceMinutes;

  @Column(name = "active", nullable = false)
  private boolean active;

  @Column(name = "effective_at", nullable = false)
  private Instant effectiveAt;

  protected CancellationPolicyJpaEntity() {}

  public CancellationPolicyJpaEntity(
      UUID id,
      int version,
      int cutoffHours,
      BigDecimal lateRate,
      BigDecimal noShowRate,
      int graceMinutes,
      boolean active,
      Instant effectiveAt) {
    this.id = id;
    this.version = version;
    this.cutoffHours = cutoffHours;
    this.lateRate = lateRate;
    this.noShowRate = noShowRate;
    this.graceMinutes = graceMinutes;
    this.active = active;
    this.effectiveAt = effectiveAt;
  }

  public UUID getId() {
    return id;
  }

  public int getVersion() {
    return version;
  }

  public int getCutoffHours() {
    return cutoffHours;
  }

  public BigDecimal getLateRate() {
    return lateRate;
  }

  public BigDecimal getNoShowRate() {
    return noShowRate;
  }

  public int getGraceMinutes() {
    return graceMinutes;
  }

  public boolean isActive() {
    return active;
  }

  public Instant getEffectiveAt() {
    return effectiveAt;
  }
}
