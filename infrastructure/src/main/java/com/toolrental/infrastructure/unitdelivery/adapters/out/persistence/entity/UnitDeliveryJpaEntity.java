package com.toolrental.infrastructure.unitdelivery.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "unit_deliveries")
public class UnitDeliveryJpaEntity {
  @EmbeddedId private UnitDeliveryJpaId id;

  @Column(name = "scheduled_at", nullable = false)
  private Instant scheduledAt;

  @Column(name = "delivered_at", nullable = true)
  private Instant deliveredAt;

  @Column(name = "outbound_condition", nullable = true, length = 500)
  private String outboundCondition;

  @Column(name = "address_line", nullable = false, length = 300)
  private String addressLine;

  @Column(name = "city", nullable = false, length = 120)
  private String city;

  @Column(name = "region", nullable = false, length = 120)
  private String region;

  @Column(name = "country", nullable = false, length = 120)
  private String country;

  protected UnitDeliveryJpaEntity() {}

  public UnitDeliveryJpaEntity(
      UnitDeliveryJpaId id,
      Instant scheduledAt,
      Instant deliveredAt,
      String outboundCondition,
      String addressLine,
      String city,
      String region,
      String country) {
    this.id = id;
    this.scheduledAt = scheduledAt;
    this.deliveredAt = deliveredAt;
    this.outboundCondition = outboundCondition;
    this.addressLine = addressLine;
    this.city = city;
    this.region = region;
    this.country = country;
  }

  public UnitDeliveryJpaId getId() {
    return id;
  }

  public Instant getScheduledAt() {
    return scheduledAt;
  }

  public Instant getDeliveredAt() {
    return deliveredAt;
  }

  public String getOutboundCondition() {
    return outboundCondition;
  }

  public String getAddressLine() {
    return addressLine;
  }

  public String getCity() {
    return city;
  }

  public String getRegion() {
    return region;
  }

  public String getCountry() {
    return country;
  }
}
