package com.toolrental.infrastructure.reservationtermination.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservation_terminations")
public class ReservationTerminationJpaEntity {
  @Id
  @Column(name = "reservation_termination_id")
  private UUID id;

  @Column(name = "reservation_id", nullable = false)
  private UUID reservationId;

  @Column(name = "termination_type", nullable = false, length = 30)
  private String type;

  @Column(name = "termination_status", nullable = false, length = 20)
  private String status;

  @Column(name = "requested_by", nullable = false)
  private UUID requestedBy;

  @Column(name = "requested_at", nullable = false)
  private Instant requestedAt;

  @Column(name = "resolved_by")
  private UUID resolvedBy;

  @Column(name = "resolved_at")
  private Instant resolvedAt;

  @Column(name = "reason", length = 500)
  private String reason;

  @Column(name = "fee_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal fee;

  @Column(name = "refund_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal refund;

  @Column(name = "outstanding_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal outstanding;

  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  @Column(name = "idempotency_key", nullable = false, length = 200)
  private String idempotencyKey;

  protected ReservationTerminationJpaEntity() {}

  public ReservationTerminationJpaEntity(
      UUID id,
      UUID reservationId,
      String type,
      String status,
      UUID requestedBy,
      Instant requestedAt,
      UUID resolvedBy,
      Instant resolvedAt,
      String reason,
      BigDecimal fee,
      BigDecimal refund,
      BigDecimal outstanding,
      String currency,
      String key) {
    this.id = id;
    this.reservationId = reservationId;
    this.type = type;
    this.status = status;
    this.requestedBy = requestedBy;
    this.requestedAt = requestedAt;
    this.resolvedBy = resolvedBy;
    this.resolvedAt = resolvedAt;
    this.reason = reason;
    this.fee = fee;
    this.refund = refund;
    this.outstanding = outstanding;
    this.currency = currency;
    idempotencyKey = key;
  }

  public UUID getId() {
    return id;
  }

  public UUID getReservationId() {
    return reservationId;
  }

  public String getType() {
    return type;
  }

  public String getStatus() {
    return status;
  }

  public UUID getRequestedBy() {
    return requestedBy;
  }

  public Instant getRequestedAt() {
    return requestedAt;
  }

  public UUID getResolvedBy() {
    return resolvedBy;
  }

  public Instant getResolvedAt() {
    return resolvedAt;
  }

  public String getReason() {
    return reason;
  }

  public BigDecimal getFee() {
    return fee;
  }

  public BigDecimal getRefund() {
    return refund;
  }

  public BigDecimal getOutstanding() {
    return outstanding;
  }

  public String getCurrency() {
    return currency;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }
}
