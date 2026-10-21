package com.toolrental.domain.reservationtermination.model.aggregate;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservationtermination.event.ReservationTerminationChanged;
import com.toolrental.domain.reservationtermination.model.valueobject.ReservationTerminationId;
import com.toolrental.domain.user.model.valueobject.UserId;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public final class ReservationTermination extends AggregateRoot {
  private static final Set<String> TYPES =
      Set.of("STANDARD", "NO_SHOW", "WITHDRAWAL", "SUPPLIER_FAILURE", "EXTERNAL_REVIEW");
  private static final Set<String> STATUSES =
      Set.of("REQUESTED", "APPLIED", "APPROVED", "REJECTED");
  private final ReservationTerminationId id;
  private final ReservationId reservationId;
  private final String type;
  private final String status;
  private final UserId requestedBy;
  private final Instant requestedAt;
  private final UserId resolvedBy;
  private final Instant resolvedAt;
  private final String reason;
  private final Money fee;
  private final Money refund;
  private final Money outstanding;
  private final String idempotencyKey;

  private ReservationTermination(
      ReservationTerminationId id,
      ReservationId reservationId,
      String type,
      String status,
      UserId requestedBy,
      Instant requestedAt,
      UserId resolvedBy,
      Instant resolvedAt,
      String reason,
      Money fee,
      Money refund,
      Money outstanding,
      String idempotencyKey) {
    this.id = Objects.requireNonNull(id);
    this.reservationId = Objects.requireNonNull(reservationId);
    if (!TYPES.contains(type) || !STATUSES.contains(status))
      throw new BusinessRuleException("Invalid reservation termination");
    if (!fee.currency().equals(refund.currency()) || !fee.currency().equals(outstanding.currency()))
      throw new BusinessRuleException("Termination amounts must use one currency");
    if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 200)
      throw new BusinessRuleException("Invalid idempotency key");
    if (reason != null && (reason.isBlank() || reason.length() > 500))
      throw new BusinessRuleException("Invalid termination reason");
    this.type = type;
    this.status = status;
    this.requestedBy = Objects.requireNonNull(requestedBy);
    this.requestedAt = Objects.requireNonNull(requestedAt);
    this.resolvedBy = resolvedBy;
    this.resolvedAt = resolvedAt;
    this.reason = reason;
    this.fee = fee;
    this.refund = refund;
    this.outstanding = outstanding;
    this.idempotencyKey = idempotencyKey;
  }

  public static ReservationTermination create(
      ReservationTerminationId id,
      ReservationId reservationId,
      String type,
      String status,
      UserId requestedBy,
      Instant requestedAt,
      UserId resolvedBy,
      Instant resolvedAt,
      String reason,
      Money fee,
      Money refund,
      Money outstanding,
      String idempotencyKey) {
    var result =
        new ReservationTermination(
            id,
            reservationId,
            type,
            status,
            requestedBy,
            requestedAt,
            resolvedBy,
            resolvedAt,
            reason,
            fee,
            refund,
            outstanding,
            idempotencyKey);
    result.recordEvent(new ReservationTerminationChanged(id, requestedAt));
    return result;
  }

  public static ReservationTermination restore(
      ReservationTerminationId id,
      ReservationId reservationId,
      String type,
      String status,
      UserId requestedBy,
      Instant requestedAt,
      UserId resolvedBy,
      Instant resolvedAt,
      String reason,
      Money fee,
      Money refund,
      Money outstanding,
      String idempotencyKey) {
    return new ReservationTermination(
        id,
        reservationId,
        type,
        status,
        requestedBy,
        requestedAt,
        resolvedBy,
        resolvedAt,
        reason,
        fee,
        refund,
        outstanding,
        idempotencyKey);
  }

  public ReservationTermination resolve(
      String next, UserId actor, Instant now, Money fee, Money refund, Money outstanding) {
    if (!"REQUESTED".equals(status) || !Set.of("APPROVED", "REJECTED").contains(next))
      throw new BusinessRuleException("Invalid withdrawal resolution");
    var result =
        new ReservationTermination(
            id,
            reservationId,
            type,
            next,
            requestedBy,
            requestedAt,
            actor,
            now,
            reason,
            fee,
            refund,
            outstanding,
            idempotencyKey);
    result.recordEvent(new ReservationTerminationChanged(id, now));
    return result;
  }

  public ReservationTerminationId id() {
    return id;
  }

  public ReservationId reservationId() {
    return reservationId;
  }

  public String type() {
    return type;
  }

  public String status() {
    return status;
  }

  public UserId requestedBy() {
    return requestedBy;
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

  public String reason() {
    return reason;
  }

  public Money fee() {
    return fee;
  }

  public Money refund() {
    return refund;
  }

  public Money outstanding() {
    return outstanding;
  }

  public String idempotencyKey() {
    return idempotencyKey;
  }
}
