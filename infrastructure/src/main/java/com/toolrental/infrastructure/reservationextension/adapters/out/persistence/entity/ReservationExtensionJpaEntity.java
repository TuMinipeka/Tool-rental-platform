package com.toolrental.infrastructure.reservationextension.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "reservation_extensions")
public class ReservationExtensionJpaEntity {
  @Id
  @Column(name = "reservation_extension_id")
  private UUID id;

  @Column(name = "reservation_id", nullable = false)
  private UUID reservationId;

  @Column(name = "requested_by", nullable = false)
  private UUID requestedBy;

  @Column(name = "requested_return_date", nullable = false)
  private LocalDate requestedReturnDate;

  @Column(name = "extension_status", nullable = false, length = 20)
  private String status;

  @Column(name = "requested_at", nullable = false)
  private Instant requestedAt;

  @Column(name = "resolved_by")
  private UUID resolvedBy;

  @Column(name = "resolved_at")
  private Instant resolvedAt;

  @Column(name = "additional_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal amount;

  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.CHAR)
  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  @Column(name = "idempotency_key", nullable = false, length = 200)
  private String key;

  protected ReservationExtensionJpaEntity() {}

  public ReservationExtensionJpaEntity(
      UUID id,
      UUID reservationId,
      UUID requestedBy,
      LocalDate returnDate,
      String status,
      Instant requestedAt,
      UUID resolvedBy,
      Instant resolvedAt,
      BigDecimal amount,
      String currency,
      String key) {
    this.id = id;
    this.reservationId = reservationId;
    this.requestedBy = requestedBy;
    requestedReturnDate = returnDate;
    this.status = status;
    this.requestedAt = requestedAt;
    this.resolvedBy = resolvedBy;
    this.resolvedAt = resolvedAt;
    this.amount = amount;
    this.currency = currency;
    this.key = key;
  }

  public UUID getId() {
    return id;
  }

  public UUID getReservationId() {
    return reservationId;
  }

  public UUID getRequestedBy() {
    return requestedBy;
  }

  public LocalDate getRequestedReturnDate() {
    return requestedReturnDate;
  }

  public String getStatus() {
    return status;
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

  public BigDecimal getAmount() {
    return amount;
  }

  public String getCurrency() {
    return currency;
  }

  public String getKey() {
    return key;
  }
}
