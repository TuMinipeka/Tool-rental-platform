package com.toolrental.infrastructure.reservation.adapters.out.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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

  @Column(name = "currency", nullable = false, length = 3, updatable = false)
  private String currency;

  @Column(name = "confirmed_at")
  private Instant confirmedAt;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "reservation_items", joinColumns = @JoinColumn(name = "reservation_id"))
  private Set<ReservationItemJpaEmbeddable> items = new LinkedHashSet<>();

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
      Set<ReservationItemJpaEmbeddable> items) {
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

  public Set<ReservationItemJpaEmbeddable> getItems() {
    return Set.copyOf(items);
  }
}
