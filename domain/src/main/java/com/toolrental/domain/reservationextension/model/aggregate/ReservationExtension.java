package com.toolrental.domain.reservationextension.model.aggregate;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservationextension.event.ReservationExtensionChanged;
import com.toolrental.domain.reservationextension.model.valueobject.ReservationExtensionId;
import com.toolrental.domain.user.model.valueobject.UserId;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

public final class ReservationExtension extends AggregateRoot {
  private static final Set<String> STATUSES = Set.of("REQUESTED", "APPROVED", "REJECTED");
  private final ReservationExtensionId id;
  private final ReservationId reservationId;
  private final UserId requestedBy;
  private final LocalDate requestedReturnDate;
  private final String status;
  private final Instant requestedAt;
  private final UserId resolvedBy;
  private final Instant resolvedAt;
  private final Money additionalAmount;
  private final String idempotencyKey;

  private ReservationExtension(
      ReservationExtensionId id,
      ReservationId reservationId,
      UserId requestedBy,
      LocalDate requestedReturnDate,
      String status,
      Instant requestedAt,
      UserId resolvedBy,
      Instant resolvedAt,
      Money additionalAmount,
      String idempotencyKey) {
    this.id = Objects.requireNonNull(id);
    this.reservationId = Objects.requireNonNull(reservationId);
    this.requestedBy = Objects.requireNonNull(requestedBy);
    this.requestedReturnDate = Objects.requireNonNull(requestedReturnDate);
    if (!STATUSES.contains(status)) throw new BusinessRuleException("Invalid extension status");
    if (("REQUESTED".equals(status) && (resolvedBy != null || resolvedAt != null))
        || (!"REQUESTED".equals(status) && (resolvedBy == null || resolvedAt == null)))
      throw new BusinessRuleException("Invalid extension resolution");
    if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 200)
      throw new BusinessRuleException("Invalid idempotency key");
    this.status = status;
    this.requestedAt = Objects.requireNonNull(requestedAt);
    this.resolvedBy = resolvedBy;
    this.resolvedAt = resolvedAt;
    this.additionalAmount = Objects.requireNonNull(additionalAmount);
    this.idempotencyKey = idempotencyKey;
  }

  public static ReservationExtension request(
      ReservationExtensionId id,
      ReservationId reservationId,
      UserId requestedBy,
      LocalDate requestedReturnDate,
      Money additionalAmount,
      String idempotencyKey,
      Instant now) {
    var result =
        new ReservationExtension(
            id,
            reservationId,
            requestedBy,
            requestedReturnDate,
            "REQUESTED",
            now,
            null,
            null,
            additionalAmount,
            idempotencyKey);
    result.recordEvent(new ReservationExtensionChanged(id, now));
    return result;
  }

  public static ReservationExtension restore(
      ReservationExtensionId id,
      ReservationId reservationId,
      UserId requestedBy,
      LocalDate requestedReturnDate,
      String status,
      Instant requestedAt,
      UserId resolvedBy,
      Instant resolvedAt,
      Money additionalAmount,
      String idempotencyKey) {
    return new ReservationExtension(
        id,
        reservationId,
        requestedBy,
        requestedReturnDate,
        status,
        requestedAt,
        resolvedBy,
        resolvedAt,
        additionalAmount,
        idempotencyKey);
  }

  public ReservationExtension resolve(String next, UserId actor, Instant now) {
    if (!"REQUESTED".equals(status) || !Set.of("APPROVED", "REJECTED").contains(next))
      throw new BusinessRuleException("Extension is not pending");
    var result =
        new ReservationExtension(
            id,
            reservationId,
            requestedBy,
            requestedReturnDate,
            next,
            requestedAt,
            actor,
            now,
            additionalAmount,
            idempotencyKey);
    result.recordEvent(new ReservationExtensionChanged(id, now));
    return result;
  }

  public ReservationExtensionId id() {
    return id;
  }

  public ReservationId reservationId() {
    return reservationId;
  }

  public UserId requestedBy() {
    return requestedBy;
  }

  public LocalDate requestedReturnDate() {
    return requestedReturnDate;
  }

  public String status() {
    return status;
  }

  public Instant requestedAt() {
    return requestedAt;
  }

  public UserId resolvedBy() {
    return resolvedBy;
  }

  public Instant resolvedAt() {
    return resolvedAt;
  }

  public Money additionalAmount() {
    return additionalAmount;
  }

  public String idempotencyKey() {
    return idempotencyKey;
  }
}
