package com.toolrental.infrastructure.refund.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refunds")
public class RefundJpaEntity {
  @Id
  @Column(name = "refund_id")
  private UUID id;

  @Column(name = "payment_id", nullable = false)
  private UUID paymentId;

  @Column(name = "reservation_termination_id")
  private UUID terminationId;

  @Column(name = "amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal amount;

  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.CHAR)
  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  @Column(name = "refund_status", nullable = false, length = 20)
  private String status;

  @Column(name = "idempotency_key", nullable = false, length = 200)
  private String key;

  @Column(name = "recorded_at", nullable = false)
  private Instant recordedAt;

  @Column(name = "confirmed_at")
  private Instant confirmedAt;

  protected RefundJpaEntity() {}

  public RefundJpaEntity(
      UUID id,
      UUID paymentId,
      UUID terminationId,
      BigDecimal amount,
      String currency,
      String status,
      String key,
      Instant recordedAt,
      Instant confirmedAt) {
    this.id = id;
    this.paymentId = paymentId;
    this.terminationId = terminationId;
    this.amount = amount;
    this.currency = currency;
    this.status = status;
    this.key = key;
    this.recordedAt = recordedAt;
    this.confirmedAt = confirmedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPaymentId() {
    return paymentId;
  }

  public UUID getTerminationId() {
    return terminationId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getCurrency() {
    return currency;
  }

  public String getStatus() {
    return status;
  }

  public String getKey() {
    return key;
  }

  public Instant getRecordedAt() {
    return recordedAt;
  }

  public Instant getConfirmedAt() {
    return confirmedAt;
  }
}
