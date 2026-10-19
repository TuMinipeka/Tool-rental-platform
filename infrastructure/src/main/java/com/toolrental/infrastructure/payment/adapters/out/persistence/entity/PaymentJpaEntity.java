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

  @Column(name = "payment_method", nullable = false, length = 30)
  private String paymentMethod;

  @Column(name = "payment_stage", nullable = false, length = 30)
  private String paymentStage;

  @Column(name = "idempotency_key", nullable = false, length = 200)
  private String idempotencyKey;

  @Column(name = "card_network", length = 20)
  private String cardNetwork;

  @Column(name = "card_last_four", length = 4)
  private String cardLastFour;

  @Column(name = "card_expiry_month")
  private Short cardExpiryMonth;

  @Column(name = "card_expiry_year")
  private Short cardExpiryYear;

  @Column(name = "adjustment_type", length = 30)
  private String adjustmentType;

  @Column(name = "adjustment_reference")
  private UUID adjustmentReference;

  protected PaymentJpaEntity() {}

  public PaymentJpaEntity(
      UUID id,
      UUID reservationId,
      String processor,
      String externalReference,
      BigDecimal amount,
      String paymentStatus,
      Instant recordedAt,
      Instant confirmedAt,
      String paymentMethod,
      String paymentStage,
      String idempotencyKey,
      String cardNetwork,
      String cardLastFour,
      Short cardExpiryMonth,
      Short cardExpiryYear,
      String adjustmentType,
      UUID adjustmentReference) {
    this.id = id;
    this.reservationId = reservationId;
    this.processor = processor;
    this.externalReference = externalReference;
    this.amount = amount;
    this.paymentStatus = paymentStatus;
    this.recordedAt = recordedAt;
    this.confirmedAt = confirmedAt;
    this.paymentMethod = paymentMethod;
    this.paymentStage = paymentStage;
    this.idempotencyKey = idempotencyKey;
    this.cardNetwork = cardNetwork;
    this.cardLastFour = cardLastFour;
    this.cardExpiryMonth = cardExpiryMonth;
    this.cardExpiryYear = cardExpiryYear;
    this.adjustmentType = adjustmentType;
    this.adjustmentReference = adjustmentReference;
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

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public String getPaymentStage() {
    return paymentStage;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public String getCardNetwork() {
    return cardNetwork;
  }

  public String getCardLastFour() {
    return cardLastFour;
  }

  public Short getCardExpiryMonth() {
    return cardExpiryMonth;
  }

  public Short getCardExpiryYear() {
    return cardExpiryYear;
  }

  public String getAdjustmentType() {
    return adjustmentType;
  }

  public UUID getAdjustmentReference() {
    return adjustmentReference;
  }
}
