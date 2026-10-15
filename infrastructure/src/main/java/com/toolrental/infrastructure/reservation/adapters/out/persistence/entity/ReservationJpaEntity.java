package com.toolrental.infrastructure.reservation.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class ReservationJpaEntity {
  @Id
  @Column(name = "reservation_id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "customer_id", nullable = false, updatable = false)
  private UUID customerId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "start_date", nullable = false, updatable = false)
  private LocalDate startDate;

  @Column(name = "return_date", nullable = false, updatable = false)
  private LocalDate returnDate;

  @Column(name = "reservation_status", nullable = false, length = 20)
  private String reservationStatus;

  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.CHAR)
  @Column(name = "currency", nullable = false, length = 3, updatable = false)
  private String currency;

  @Column(name = "confirmed_at")
  private Instant confirmedAt;

  @jakarta.persistence.Transient
  private Set<
          com.toolrental.infrastructure.reservationitem.adapters.out.persistence.entity
              .ReservationItemJpaEntity>
      items = new LinkedHashSet<>();

  @Column(name = "expires_at")
  private Instant expiresAt;

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void preserveExpiresAt(Instant value) {
    expiresAt = value;
  }

  protected ReservationJpaEntity() {}

  public ReservationJpaEntity(
      UUID id,
      UUID customerId,
      Instant createdAt,
      LocalDate startDate,
      LocalDate returnDate,
      String reservationStatus,
      String currency,
      Instant confirmedAt,
      Set<
              com.toolrental.infrastructure.reservationitem.adapters.out.persistence.entity
                  .ReservationItemJpaEntity>
          items) {
    this.id = id;
    this.customerId = customerId;
    this.createdAt = createdAt;
    this.startDate = startDate;
    this.returnDate = returnDate;
    this.reservationStatus = reservationStatus;
    this.currency = currency;
    this.confirmedAt = confirmedAt;
    this.items = new LinkedHashSet<>(items);
  }

  public void attachItems(
      Set<
              com.toolrental.infrastructure.reservationitem.adapters.out.persistence.entity
                  .ReservationItemJpaEntity>
          values) {
    items = new LinkedHashSet<>(values);
  }

  public UUID getId() {
    return id;
  }

  public UUID getCustomerId() {
    return customerId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getReturnDate() {
    return returnDate;
  }

  public String getReservationStatus() {
    return reservationStatus;
  }

  public String getCurrency() {
    return currency;
  }

  public Instant getConfirmedAt() {
    return confirmedAt;
  }

  public Set<
          com.toolrental.infrastructure.reservationitem.adapters.out.persistence.entity
              .ReservationItemJpaEntity>
      getItems() {
    return Set.copyOf(items);
  }
}
