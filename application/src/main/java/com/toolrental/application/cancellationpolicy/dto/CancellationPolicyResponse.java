package com.toolrental.application.cancellationpolicy.dto;

import com.toolrental.domain.cancellationpolicy.model.aggregate.CancellationPolicy;
import com.toolrental.domain.cancellationpolicy.model.valueobject.CancellationPolicyId;
import java.math.BigDecimal;
import java.time.Instant;

public record CancellationPolicyResponse(
    CancellationPolicyId id,
    int version,
    int cutoffHours,
    BigDecimal lateCancellationRate,
    BigDecimal noShowRate,
    int noShowGraceMinutes,
    boolean active,
    Instant effectiveAt) {
  public static CancellationPolicyResponse from(CancellationPolicy value) {
    return new CancellationPolicyResponse(
        value.id(),
        value.version(),
        value.cutoffHours(),
        value.lateCancellationRate(),
        value.noShowRate(),
        value.noShowGraceMinutes(),
        value.active(),
        value.effectiveAt());
  }
}
