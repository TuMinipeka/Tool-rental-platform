package com.toolrental.infrastructure.payment.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class PaymentJpaEntity {
  @Id
  @Column(name = "payment_id")
  private UUID id;

  @Column(name = "reservation_id", nullable = false)
  private UUID reservationId;

  @Column(name = "processor", nullable = false, length = 80)
  private String processor;

  @Column(name = "external_reference", nullable = false, length = 200)
  private String externalReference;

  @Column(name = "amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal amount;

  @Column(name = "payment_status", nullable = false, length = 20)
  private String paymentStatus;

  @Column(name = "recorded_at", nullable = false)
  private Instant recordedAt;

  @Column(name = "confirmed_at", nullable = true)
  private Instant confirmedAt;

  protected PaymentJpaEntity() {}

  public PaymentJpaEntity(
      UUID id,
      UUID reservationId,
      String processor,
      String externalReference,
      BigDecimal amount,
      String paymentStatus,
      Instant recordedAt,
      Instant confirmedAt) {
    this.id = id;
    this.reservationId = reservationId;
    this.processor = processor;
    this.externalReference = externalReference;
    this.amount = amount;
    this.paymentStatus = paymentStatus;
    this.recordedAt = recordedAt;
    this.confirmedAt = confirmedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getReservationId() {
    return reservationId;
  }

  public String getProcessor() {
    return processor;
  }

  public String getExternalReference() {
    return externalReference;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getPaymentStatus() {
    return paymentStatus;
  }

  public Instant getRecordedAt() {
    return recordedAt;
  }

  public Instant getConfirmedAt() {
    return confirmedAt;
  }
}
