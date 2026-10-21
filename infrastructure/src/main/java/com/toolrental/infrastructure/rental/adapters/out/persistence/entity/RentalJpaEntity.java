package com.toolrental.infrastructure.rental.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rentals")
public class RentalJpaEntity {
  @Id
  @Column(name = "reservation_id")
  private UUID id;

  @Column(name = "started_at", nullable = false)
  private Instant startedAt;

  @Column(name = "rental_status", nullable = false, length = 20)
  private String rentalStatus;

  protected RentalJpaEntity() {}

  public RentalJpaEntity(UUID id, Instant startedAt, String rentalStatus) {
    this.id = id;
    this.startedAt = startedAt;
    this.rentalStatus = rentalStatus;
  }

  public UUID getId() {
    return id;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public String getRentalStatus() {
    return rentalStatus;
  }
}
