package com.toolrental.domain.unitdelivery.model.aggregate;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.unitdelivery.event.UnitDeliveryChanged;
import com.toolrental.domain.unitdelivery.model.valueobject.UnitDeliveryId;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class UnitDelivery extends AggregateRoot {
  private final UnitDeliveryId id;
  private final Instant scheduledAt;
  private final Instant deliveredAt;
  private final String outboundCondition;
  private final String addressLine;
  private final String city;
  private final String region;
  private final String country;
  private final String fulfillmentMethod;
  private final String deliveryStatus;
  private final String failureType;
  private final String failureReason;
  private final Instant failedAt;

  private UnitDelivery(
      UnitDeliveryId id,
      Instant scheduledAt,
      Instant deliveredAt,
      String outboundCondition,
      String addressLine,
      String city,
      String region,
      String country,
      String fulfillmentMethod,
      String deliveryStatus,
      String failureType,
      String failureReason,
      Instant failedAt) {
    Objects.requireNonNull(id);
    Objects.requireNonNull(scheduledAt, "scheduledAt is required");
    if (outboundCondition != null
        && (outboundCondition.isBlank() || outboundCondition.length() > 500))
      throw new BusinessRuleException("Invalid outboundCondition");
    Objects.requireNonNull(addressLine, "addressLine is required");
    if (addressLine != null && (addressLine.isBlank() || addressLine.length() > 300))
      throw new BusinessRuleException("Invalid addressLine");
    Objects.requireNonNull(city, "city is required");
    if (city != null && (city.isBlank() || city.length() > 120))
      throw new BusinessRuleException("Invalid city");
    Objects.requireNonNull(region, "region is required");
    if (region != null && (region.isBlank() || region.length() > 120))
      throw new BusinessRuleException("Invalid region");
    Objects.requireNonNull(country, "country is required");
    if (country != null && (country.isBlank() || country.length() > 120))
      throw new BusinessRuleException("Invalid country");
    if (!"CO".equalsIgnoreCase(country) && !"COLOMBIA".equalsIgnoreCase(country))
      throw new BusinessRuleException("Deliveries are restricted to Colombia");
    if (deliveredAt != null && (deliveredAt.isBefore(scheduledAt) || outboundCondition == null))
      throw new BusinessRuleException("Delivery requires valid time and condition");
    this.id = id;
    this.scheduledAt = scheduledAt;
    this.deliveredAt = deliveredAt;
    this.outboundCondition = outboundCondition;
    this.addressLine = addressLine;
    this.city = city;
    this.region = region;
    this.country = country;
    this.fulfillmentMethod = Objects.requireNonNull(fulfillmentMethod);
    this.deliveryStatus = Objects.requireNonNull(deliveryStatus);
    if (!java.util.Set.of("CUSTOMER_PICKUP", "SUPPLIER_DELIVERY").contains(fulfillmentMethod)
        || !java.util.Set.of("SCHEDULED", "COMPLETED", "FAILED").contains(deliveryStatus))
      throw new BusinessRuleException("Invalid delivery workflow state");
    if ("FAILED".equals(deliveryStatus)
        && (!java.util.Set.of("CUSTOMER_ABSENCE", "SUPPLIER_RESPONSIBILITY", "EXTERNAL_CAUSE")
                .contains(failureType)
            || failureReason == null
            || failureReason.isBlank()
            || failureReason.length() > 500
            || failedAt == null)) throw new BusinessRuleException("Invalid delivery failure");
    this.failureType = failureType;
    this.failureReason = failureReason;
    this.failedAt = failedAt;
  }

  public static UnitDelivery restore(
      UnitDeliveryId id,
      Instant scheduledAt,
      Instant deliveredAt,
      String outboundCondition,
      String addressLine,
      String city,
      String region,
      String country) {
    return new UnitDelivery(
        id,
        scheduledAt,
        deliveredAt,
        outboundCondition,
        addressLine,
        city,
        region,
        country,
        "SUPPLIER_DELIVERY",
        deliveredAt == null ? "SCHEDULED" : "COMPLETED",
        null,
        null,
        null);
  }

  public static UnitDelivery restore(
      UnitDeliveryId id,
      Instant scheduledAt,
      Instant deliveredAt,
      String outboundCondition,
      String addressLine,
      String city,
      String region,
      String country,
      String fulfillmentMethod,
      String deliveryStatus,
      String failureType,
      String failureReason,
      Instant failedAt) {
    return new UnitDelivery(
        id,
        scheduledAt,
        deliveredAt,
        outboundCondition,
        addressLine,
        city,
        region,
        country,
        fulfillmentMethod,
        deliveryStatus,
        failureType,
        failureReason,
        failedAt);
  }

  public static UnitDelivery register(
      UnitDeliveryId id,
      Instant scheduledAt,
      Instant deliveredAt,
      String outboundCondition,
      String addressLine,
      String city,
      String region,
      String country,
      Clock clock) {
    return register(
        id,
        scheduledAt,
        deliveredAt,
        outboundCondition,
        addressLine,
        city,
        region,
        country,
        "SUPPLIER_DELIVERY",
        clock);
  }

  public static UnitDelivery register(
      UnitDeliveryId id,
      Instant scheduledAt,
      Instant deliveredAt,
      String outboundCondition,
      String addressLine,
      String city,
      String region,
      String country,
      String fulfillmentMethod,
      Clock clock) {
    var result =
        new UnitDelivery(
            id,
            scheduledAt,
            deliveredAt,
            outboundCondition,
            addressLine,
            city,
            region,
            country,
            fulfillmentMethod,
            deliveredAt == null ? "SCHEDULED" : "COMPLETED",
            null,
            null,
            null);
    result.recordEvent(new UnitDeliveryChanged(id, Instant.now(clock)));
    return result;
  }

  public UnitDelivery complete(String condition, Clock clock) {
    if (deliveredAt != null) throw new BusinessRuleException("Delivery already completed");
    if (!"SCHEDULED".equals(deliveryStatus))
      throw new BusinessRuleException("Delivery is not scheduled");
    var result =
        new UnitDelivery(
            id,
            scheduledAt,
            Instant.now(clock),
            condition,
            addressLine,
            city,
            region,
            country,
            fulfillmentMethod,
            "COMPLETED",
            null,
            null,
            null);
    result.recordEvent(new UnitDeliveryChanged(id, Instant.now(clock)));
    return result;
  }

  public UnitDelivery fail(String type, String reason, Clock clock) {
    if (!"SCHEDULED".equals(deliveryStatus))
      throw new BusinessRuleException("Delivery is not scheduled");
    var now = Instant.now(clock);
    var result =
        new UnitDelivery(
            id,
            scheduledAt,
            null,
            null,
            addressLine,
            city,
            region,
            country,
            fulfillmentMethod,
            "FAILED",
            type,
            reason,
            now);
    result.recordEvent(new UnitDeliveryChanged(id, now));
    return result;
  }

  public UnitDeliveryId id() {
    return id;
  }

  public Instant scheduledAt() {
    return scheduledAt;
  }

  public Instant deliveredAt() {
    return deliveredAt;
  }

  public String outboundCondition() {
    return outboundCondition;
  }

  public String addressLine() {
    return addressLine;
  }

  public String city() {
    return city;
  }

  public String region() {
    return region;
  }

  public String country() {
    return country;
  }

  public String fulfillmentMethod() {
    return fulfillmentMethod;
  }

  public String deliveryStatus() {
    return deliveryStatus;
  }

  public String failureType() {
    return failureType;
  }

  public String failureReason() {
    return failureReason;
  }

  public Instant failedAt() {
    return failedAt;
  }
}
