package com.toolrental.domain.reservation.model.aggregate;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.customer.model.valueobject.CustomerId;
import com.toolrental.domain.reservation.event.ReservationCancelled;
import com.toolrental.domain.reservation.event.ReservationConfirmed;
import com.toolrental.domain.reservation.event.ReservationRejected;
import com.toolrental.domain.reservation.event.ReservationRequested;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import com.toolrental.domain.reservationitem.model.entity.ReservationLine;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Reservation extends AggregateRoot {
  private final ReservationId id;
  private final CustomerId customerId;
  private final ReservationPeriod period;
  private final List<ReservationLine> items;
  private final Instant createdAt;
  private ReservationStatus status = ReservationStatus.PENDING;
  private ReservationConfirmed confirmation;
  private Instant expiresAt;

  private Reservation(
      CustomerId customer, ReservationPeriod period, List<ReservationLine> items, Clock clock) {
    id = ReservationId.generate();
    customerId = Objects.requireNonNull(customer);
    this.period = Objects.requireNonNull(period);
    this.items = validate(items);
    createdAt = Instant.now(clock);
  }

  private static List<ReservationLine> validate(List<ReservationLine> items) {
    var copy = List.copyOf(items);
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

  public static Reservation request(
      CustomerId customer, ReservationPeriod period, List<ReservationLine> items, Clock clock) {
    var reservation = new Reservation(customer, period, items, clock);
    reservation.recordEvent(new ReservationRequested(reservation.id, reservation.createdAt));
    return reservation;
  }

  /** Returns a confirmed copy so the atomic port can replace the pending version on commit. */
  public Reservation confirmWithRates(List<ReservationLine> currentRates, Clock clock) {
    requirePending();
    var currentItems = validate(currentRates);
    var ids =
        items.stream()
            .map(ReservationLine::equipmentUnitId)
            .collect(java.util.stream.Collectors.toSet());
    if (!ids.equals(
            currentItems.stream()
                .map(ReservationLine::equipmentUnitId)
                .collect(java.util.stream.Collectors.toSet()))
        || !supplierId().equals(currentItems.getFirst().supplierId()))
      throw new BusinessRuleException("Confirmation cannot change units or supplier");
    if (!items
        .getFirst()
        .dailyRate()
        .currency()
        .equals(currentItems.getFirst().dailyRate().currency()))
      throw new BusinessRuleException("Confirmation cannot change the reservation currency");
    var event = new ReservationConfirmed(id, Instant.now(clock));
    var confirmed = new Reservation(this, currentItems, ReservationStatus.CONFIRMED, event);
    confirmed.recordEvent(event);
    return confirmed;
  }

  private Reservation(
      Reservation original,
      List<ReservationLine> currentItems,
      ReservationStatus status,
      ReservationConfirmed confirmation) {
    expiresAt = original.expiresAt;
    id = original.id;
    customerId = original.customerId;
    period = original.period;
    createdAt = original.createdAt;
    items = currentItems;
    this.status = status;
    this.confirmation = confirmation;
  }

  public Reservation copy() {
    return new Reservation(this, items, status, confirmation);
  }

  /** Restores persisted state without emitting new domain events. */
  public static Reservation restore(
      ReservationId id,
      CustomerId customer,
      ReservationPeriod period,
      List<ReservationLine> items,
      Instant createdAt,
      ReservationStatus status,
      Instant confirmedAt) {
    return new Reservation(id, customer, period, items, createdAt, status, confirmedAt);
  }

  private Reservation(
      ReservationId id,
      CustomerId customer,
      ReservationPeriod period,
      List<ReservationLine> items,
      Instant createdAt,
      ReservationStatus status,
      Instant confirmedAt) {
    this.id = Objects.requireNonNull(id);
    this.customerId = Objects.requireNonNull(customer);
    this.period = Objects.requireNonNull(period);
    this.items = validate(items);
    this.createdAt = Objects.requireNonNull(createdAt);
    this.status = Objects.requireNonNull(status);
    if (status == ReservationStatus.CONFIRMED && confirmedAt == null)
      throw new BusinessRuleException("A confirmed reservation requires a confirmation date");
    if ((status == ReservationStatus.PENDING || status == ReservationStatus.REJECTED)
        && confirmedAt != null)
      throw new BusinessRuleException(
          "Reservation status is incompatible with the confirmation date");
    this.confirmation = confirmedAt == null ? null : new ReservationConfirmed(id, confirmedAt);
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
    var result = new Reservation(id, customer, period, items, createdAt, status, confirmedAt);
    result.expiresAt = expiresAt;
    return result;
  }

  public Instant expiresAt() {
    return expiresAt;
  }

  public void reject(Clock clock) {
    requirePending();
    var event = new ReservationRejected(id, Instant.now(clock));
    status = ReservationStatus.REJECTED;
    recordEvent(event);
  }

  public void cancel(Clock clock) {
    if (status != ReservationStatus.PENDING && status != ReservationStatus.CONFIRMED)
      throw new BusinessRuleException("A terminal reservation cannot change state");
    var event = new ReservationCancelled(id, Instant.now(clock));
    status = ReservationStatus.CANCELLED;
    recordEvent(event);
  }

  private void requirePending() {
    if (status != ReservationStatus.PENDING)
      throw new BusinessRuleException("Reservation is not pending");
  }

  public Money total() {
    var sum = new Money(java.math.BigDecimal.ZERO, items.getFirst().dailyRate().currency());
    for (var item : items) sum = sum.add(item.dailyRate().multiply(period.billableDays()));
    return sum;
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

  public Optional<ReservationConfirmed> confirmation() {
    return Optional.ofNullable(confirmation);
  }
}
