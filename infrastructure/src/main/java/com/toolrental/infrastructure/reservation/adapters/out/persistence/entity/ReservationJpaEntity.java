package com.toolrental.infrastructure.reservation.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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

  @Column(name = "scheduled_fulfillment_at", nullable = false)
  private Instant scheduledFulfillmentAt;

  @Column(name = "fulfillment_method", nullable = false, length = 30)
  private String fulfillmentMethod;

  @Column(name = "delivery_rate_id")
  private UUID deliveryRateId;

  @Column(name = "cancellation_policy_id", nullable = false)
  private UUID cancellationPolicyId;

  @Column(name = "delivery_fee", nullable = false, precision = 19, scale = 4)
  private BigDecimal deliveryFee;

  @Column(name = "effective_return_date", nullable = false)
  private LocalDate effectiveReturnDate;

  @Column(name = "delivery_address_line", length = 300)
  private String deliveryAddressLine;

  @Column(name = "delivery_city", length = 120)
  private String deliveryCity;

  @Column(name = "delivery_region", length = 120)
  private String deliveryRegion;

  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.CHAR)
  @Column(name = "delivery_country_code", length = 2)
  private String deliveryCountryCode;

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void preserveExpiresAt(Instant value) {
    expiresAt = value;
  }

  public void preserveCommercialTerms(
      Instant scheduledAt,
      String method,
      UUID rateId,
      UUID policyId,
      BigDecimal fee,
      LocalDate effectiveReturn,
      String addressLine,
      String city,
      String region,
      String countryCode) {
    scheduledFulfillmentAt = scheduledAt;
    fulfillmentMethod = method;
    deliveryRateId = rateId;
    cancellationPolicyId = policyId;
    deliveryFee = fee;
    effectiveReturnDate = effectiveReturn;
    deliveryAddressLine = addressLine;
    deliveryCity = city;
    deliveryRegion = region;
    deliveryCountryCode = countryCode;
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

  public Instant getScheduledFulfillmentAt() {
    return scheduledFulfillmentAt;
  }

  public String getFulfillmentMethod() {
    return fulfillmentMethod;
  }

  public UUID getDeliveryRateId() {
    return deliveryRateId;
  }

  public UUID getCancellationPolicyId() {
    return cancellationPolicyId;
  }

  public BigDecimal getDeliveryFee() {
    return deliveryFee;
  }

  public LocalDate getEffectiveReturnDate() {
    return effectiveReturnDate;
  }

  public String getDeliveryAddressLine() {
    return deliveryAddressLine;
  }

  public String getDeliveryCity() {
    return deliveryCity;
  }

  public String getDeliveryRegion() {
    return deliveryRegion;
  }

  public String getDeliveryCountryCode() {
    return deliveryCountryCode;
  }
}
