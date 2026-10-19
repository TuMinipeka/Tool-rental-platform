package com.toolrental.infrastructure.deliveryrate.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "delivery_rates")
public class DeliveryRateJpaEntity {
  @Id
  @Column(name = "delivery_rate_id")
  private UUID id;

  @Column(name = "rate_version", nullable = false)
  private int version;

  @Column(name = "amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal price;

  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.CHAR)
  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  @Column(name = "active", nullable = false)
  private boolean active;

  @Column(name = "effective_at", nullable = false)
  private Instant effectiveAt;

  protected DeliveryRateJpaEntity() {}

  public DeliveryRateJpaEntity(
      UUID id,
      int version,
      BigDecimal price,
      String currency,
      boolean active,
      Instant effectiveAt) {
    this.id = id;
    this.version = version;
    this.price = price;
    this.currency = currency;
    this.active = active;
    this.effectiveAt = effectiveAt;
  }

  public UUID getId() {
    return id;
  }

  public int getVersion() {
    return version;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public String getCurrency() {
    return currency;
  }

  public boolean isActive() {
    return active;
  }

  public Instant getEffectiveAt() {
    return effectiveAt;
  }
}
