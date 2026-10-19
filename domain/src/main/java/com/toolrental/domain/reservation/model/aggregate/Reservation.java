package com.toolrental.domain.reservation.model.aggregate;

import com.toolrental.domain.cancellationpolicy.model.valueobject.CancellationPolicyId;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.customer.model.valueobject.CustomerId;
import com.toolrental.domain.deliveryrate.model.valueobject.DeliveryRateId;
import com.toolrental.domain.reservation.event.ReservationCancelled;
import com.toolrental.domain.reservation.event.ReservationConfirmed;
import com.toolrental.domain.reservation.event.ReservationRejected;
import com.toolrental.domain.reservation.event.ReservationRequested;
import com.toolrental.domain.reservation.model.valueobject.DeliveryAddress;
import com.toolrental.domain.reservation.model.valueobject.FulfillmentMethod;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import com.toolrental.domain.reservationitem.model.entity.ReservationLine;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class Reservation extends AggregateRoot {
  private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Bogota");
  private static final CancellationPolicyId LEGACY_POLICY =
      new CancellationPolicyId(UUID.fromString("00000000-0000-0000-0000-000000000203"));

  private final ReservationId id;
  private final CustomerId customerId;
  private final ReservationPeriod period;
  private final List<ReservationLine> items;
  private final Instant createdAt;
  private final Instant scheduledFulfillmentAt;
  private final FulfillmentMethod fulfillmentMethod;
  private final DeliveryAddress deliveryAddress;
  private final DeliveryRateId deliveryRateId;
  private final CancellationPolicyId cancellationPolicyId;
  private final Money deliveryFee;
  private ReservationStatus status;
  private ReservationConfirmed confirmation;
  private Instant expiresAt;
  private java.time.LocalDate effectiveReturnDate;

  private Reservation(
      ReservationId id,
      CustomerId customerId,
      ReservationPeriod period,
      List<ReservationLine> items,
      Instant createdAt,
      ReservationStatus status,
      Instant confirmedAt,
      Instant expiresAt,
      Instant scheduledFulfillmentAt,
      FulfillmentMethod fulfillmentMethod,
      DeliveryAddress deliveryAddress,
      DeliveryRateId deliveryRateId,
      CancellationPolicyId cancellationPolicyId,
      Money deliveryFee,
      java.time.LocalDate effectiveReturnDate) {
    this.id = Objects.requireNonNull(id);
    this.customerId = Objects.requireNonNull(customerId);
    this.period = Objects.requireNonNull(period);
    this.items = validate(items);
    this.createdAt = Objects.requireNonNull(createdAt);
    this.status = Objects.requireNonNull(status);
    confirmation = confirmedAt == null ? null : new ReservationConfirmed(id, confirmedAt);
    this.expiresAt = expiresAt;
    this.scheduledFulfillmentAt = Objects.requireNonNull(scheduledFulfillmentAt);
    this.fulfillmentMethod = Objects.requireNonNull(fulfillmentMethod);
    this.deliveryAddress = deliveryAddress;
    this.deliveryRateId = deliveryRateId;
    this.cancellationPolicyId = Objects.requireNonNull(cancellationPolicyId);
    this.deliveryFee = Objects.requireNonNull(deliveryFee);
    this.effectiveReturnDate = Objects.requireNonNull(effectiveReturnDate);
    validateFulfillment();
    validateState();
  }

  private static List<ReservationLine> validate(List<ReservationLine> values) {
    var copy = List.copyOf(values);
    if (copy.isEmpty())
      throw new BusinessRuleException("A reservation requires at least one equipment unit");
    var first = copy.getFirst();
    var equipmentUnits = new HashSet<>();
    for (var item : copy) {
      if (!equipmentUnits.add(item.equipmentUnitId()))
        throw new BusinessRuleException("An equipment unit cannot be repeated");
      if (!first.supplierId().equals(item.supplierId()))
        throw new BusinessRuleException("All reservation items must belong to one supplier");
      if (!first.dailyRate().currency().equals(item.dailyRate().currency()))
        throw new BusinessRuleException("All reservation items must use one currency");
    }
    return copy;
  }

  private void validateFulfillment() {
    if (!deliveryFee.currency().equals(items.getFirst().dailyRate().currency()))
      throw new BusinessRuleException("Delivery and rental must use one currency");
    if (fulfillmentMethod == FulfillmentMethod.CUSTOMER_PICKUP) {
      if (deliveryAddress != null || deliveryRateId != null || deliveryFee.amount().signum() != 0)
        throw new BusinessRuleException("Customer pickup must be free and have no address");
    } else if (deliveryAddress == null || deliveryRateId == null) {
      throw new BusinessRuleException("Supplier delivery requires an address and rate");
    }
    if (effectiveReturnDate.isBefore(period.returnDate()))
      throw new BusinessRuleException("Effective return date cannot shorten the reservation");
  }

  private void validateState() {
    var confirmed =
        status == ReservationStatus.CONFIRMED
            || status == ReservationStatus.CANCELLATION_REVIEW
            || status == ReservationStatus.NO_SHOW;
    if (confirmed && confirmation == null)
      throw new BusinessRuleException("Reservation status requires a confirmation date");
    if ((status == ReservationStatus.PENDING || status == ReservationStatus.REJECTED)
        && confirmation != null)
      throw new BusinessRuleException("Reservation status is incompatible with confirmation");
  }

  public static Reservation request(
      CustomerId customer, ReservationPeriod period, List<ReservationLine> items, Clock clock) {
    var currency =
        items.isEmpty() ? Currency.getInstance("COP") : items.getFirst().dailyRate().currency();
    return request(
        customer,
        period,
        items,
        period.startDate().atTime(8, 0).atZone(BUSINESS_ZONE).toInstant(),
        FulfillmentMethod.CUSTOMER_PICKUP,
        null,
        null,
        LEGACY_POLICY,
        new Money(BigDecimal.ZERO, currency),
        clock);
  }

  public static Reservation request(
      CustomerId customer,
      ReservationPeriod period,
      List<ReservationLine> items,
      Instant scheduledFulfillmentAt,
      FulfillmentMethod fulfillmentMethod,
      DeliveryAddress deliveryAddress,
      DeliveryRateId deliveryRateId,
      CancellationPolicyId cancellationPolicyId,
      Money deliveryFee,
      Clock clock) {
    var now = Instant.now(clock);
    if (scheduledFulfillmentAt.isBefore(now))
      throw new BusinessRuleException("Scheduled fulfillment cannot be in the past");
    var reservation =
        new Reservation(
            ReservationId.generate(),
            customer,
            period,
            items,
            now,
            ReservationStatus.PENDING,
            null,
            null,
            scheduledFulfillmentAt,
            fulfillmentMethod,
            deliveryAddress,
            deliveryRateId,
            cancellationPolicyId,
            deliveryFee,
            period.returnDate());
    reservation.recordEvent(new ReservationRequested(reservation.id, now));
    return reservation;
  }

  public Reservation confirmWithRates(List<ReservationLine> currentRates, Clock clock) {
    requirePending();
    var currentItems = validate(currentRates);
    var ids =
        items.stream()
            .map(ReservationLine::equipmentUnitId)
            .collect(java.util.stream.Collectors.toSet());
    var currentIds =
        currentItems.stream()
            .map(ReservationLine::equipmentUnitId)
            .collect(java.util.stream.Collectors.toSet());
    if (!ids.equals(currentIds) || !supplierId().equals(currentItems.getFirst().supplierId()))
      throw new BusinessRuleException("Confirmation cannot change units or supplier");
    var event = new ReservationConfirmed(id, Instant.now(clock));
    var confirmed = copyWith(currentItems, ReservationStatus.CONFIRMED, event.occurredOn());
    confirmed.recordEvent(event);
    return confirmed;
  }

  private Reservation copyWith(
      List<ReservationLine> currentItems, ReservationStatus next, Instant confirmedAt) {
    return new Reservation(
        id,
        customerId,
        period,
        currentItems,
        createdAt,
        next,
        confirmedAt,
        expiresAt,
        scheduledFulfillmentAt,
        fulfillmentMethod,
        deliveryAddress,
        deliveryRateId,
        cancellationPolicyId,
        deliveryFee,
        effectiveReturnDate);
  }

  public Reservation copy() {
    return copyWith(items, status, confirmation == null ? null : confirmation.occurredOn());
  }

  public static Reservation restore(
      ReservationId id,
      CustomerId customer,
      ReservationPeriod period,
      List<ReservationLine> items,
      Instant createdAt,
      ReservationStatus status,
      Instant confirmedAt) {
    return restore(id, customer, period, items, createdAt, status, confirmedAt, null);
  }

  public static Reservation restore(
      ReservationId id,
      CustomerId customer,
      ReservationPeriod period,
      List<ReservationLine> items,
      Instant createdAt,
      ReservationStatus status,
      Instant confirmedAt,
      Instant expiresAt) {
    var currency = items.getFirst().dailyRate().currency();
    return restore(
        id,
        customer,
        period,
        items,
        createdAt,
        status,
        confirmedAt,
        expiresAt,
        period.startDate().atTime(8, 0).atZone(BUSINESS_ZONE).toInstant(),
        FulfillmentMethod.CUSTOMER_PICKUP,
        null,
        null,
        LEGACY_POLICY,
        new Money(BigDecimal.ZERO, currency),
        period.returnDate());
  }

  public static Reservation restore(
      ReservationId id,
      CustomerId customer,
      ReservationPeriod period,
      List<ReservationLine> items,
      Instant createdAt,
      ReservationStatus status,
      Instant confirmedAt,
      Instant expiresAt,
      Instant scheduledFulfillmentAt,
      FulfillmentMethod fulfillmentMethod,
      DeliveryAddress deliveryAddress,
      DeliveryRateId deliveryRateId,
      CancellationPolicyId cancellationPolicyId,
      Money deliveryFee,
      java.time.LocalDate effectiveReturnDate) {
    return new Reservation(
        id,
        customer,
        period,
        items,
        createdAt,
        status,
        confirmedAt,
        expiresAt,
        scheduledFulfillmentAt,
        fulfillmentMethod,
        deliveryAddress,
        deliveryRateId,
        cancellationPolicyId,
        deliveryFee,
        effectiveReturnDate);
  }

  public void reject(Clock clock) {
    requirePending();
    status = ReservationStatus.REJECTED;
    recordEvent(new ReservationRejected(id, Instant.now(clock)));
  }

  public void cancel(Clock clock) {
    if (status != ReservationStatus.PENDING
        && status != ReservationStatus.CONFIRMED
        && status != ReservationStatus.CANCELLATION_REVIEW)
      throw new BusinessRuleException("A terminal reservation cannot change state");
    status = ReservationStatus.CANCELLED;
    recordEvent(new ReservationCancelled(id, Instant.now(clock)));
  }

  public void requestWithdrawalReview() {
    if (status != ReservationStatus.CONFIRMED)
      throw new BusinessRuleException("Only a confirmed reservation can enter withdrawal review");
    status = ReservationStatus.CANCELLATION_REVIEW;
  }

  public void rejectWithdrawal() {
    if (status != ReservationStatus.CANCELLATION_REVIEW)
      throw new BusinessRuleException("Reservation is not under withdrawal review");
    status = ReservationStatus.CONFIRMED;
  }

  public void markNoShow() {
    if (status != ReservationStatus.CONFIRMED)
      throw new BusinessRuleException("Only a confirmed reservation can become a no-show");
    status = ReservationStatus.NO_SHOW;
  }

  public void extendTo(java.time.LocalDate requestedReturnDate) {
    if (status != ReservationStatus.CONFIRMED)
      throw new BusinessRuleException("Only a confirmed reservation can be extended");
    if (!requestedReturnDate.isAfter(effectiveReturnDate))
      throw new BusinessRuleException("Extension must increase the effective return date");
    effectiveReturnDate = requestedReturnDate;
  }

  private void requirePending() {
    if (status != ReservationStatus.PENDING)
      throw new BusinessRuleException("Reservation is not pending");
  }

  public Money rentalSubtotal() {
    var sum = new Money(BigDecimal.ZERO, items.getFirst().dailyRate().currency());
    for (var item : items) sum = sum.add(item.dailyRate().multiply(period.billableDays()));
    return sum;
  }

  public Money total() {
    return rentalSubtotal().add(deliveryFee);
  }

  public ReservationPeriod effectivePeriod() {
    return new ReservationPeriod(period.startDate(), effectiveReturnDate);
  }

  public ReservationId id() {
    return id;
  }

  public CustomerId customerId() {
    return customerId;
  }

  public SupplierId supplierId() {
    return items.getFirst().supplierId();
  }

  public ReservationPeriod period() {
    return period;
  }

  public List<ReservationLine> items() {
    return items;
  }

  public ReservationStatus status() {
    return status;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant expiresAt() {
    return expiresAt;
  }

  public Instant scheduledFulfillmentAt() {
    return scheduledFulfillmentAt;
  }

  public FulfillmentMethod fulfillmentMethod() {
    return fulfillmentMethod;
  }

  public Optional<DeliveryAddress> deliveryAddress() {
    return Optional.ofNullable(deliveryAddress);
  }

  public Optional<DeliveryRateId> deliveryRateId() {
    return Optional.ofNullable(deliveryRateId);
  }

  public CancellationPolicyId cancellationPolicyId() {
    return cancellationPolicyId;
  }

  public Money deliveryFee() {
    return deliveryFee;
  }

  public java.time.LocalDate effectiveReturnDate() {
    return effectiveReturnDate;
  }

  public Optional<ReservationConfirmed> confirmation() {
    return Optional.ofNullable(confirmation);
  }
}
