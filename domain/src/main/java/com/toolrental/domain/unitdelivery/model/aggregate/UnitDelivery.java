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

  private UnitDelivery(
      UnitDeliveryId id,
      Instant scheduledAt,
      Instant deliveredAt,
      String outboundCondition,
      String addressLine,
      String city,
      String region,
      String country) {
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
        id, scheduledAt, deliveredAt, outboundCondition, addressLine, city, region, country);
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
    var result =
        new UnitDelivery(
            id, scheduledAt, deliveredAt, outboundCondition, addressLine, city, region, country);
    result.recordEvent(new UnitDeliveryChanged(id, Instant.now(clock)));
    return result;
  }

  public UnitDelivery complete(String condition, Clock clock) {
    if (deliveredAt != null) throw new BusinessRuleException("Delivery already completed");
    var result =
        new UnitDelivery(
            id, scheduledAt, Instant.now(clock), condition, addressLine, city, region, country);
    result.recordEvent(new UnitDeliveryChanged(id, Instant.now(clock)));
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
}
