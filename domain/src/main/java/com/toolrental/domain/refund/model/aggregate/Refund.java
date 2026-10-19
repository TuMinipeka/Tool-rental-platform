package com.toolrental.domain.refund.model.aggregate;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.payment.model.valueobject.PaymentId;
import com.toolrental.domain.refund.event.RefundChanged;
import com.toolrental.domain.refund.model.valueobject.RefundId;
import com.toolrental.domain.reservationtermination.model.valueobject.ReservationTerminationId;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public final class Refund extends AggregateRoot {
  private final RefundId id;
  private final PaymentId paymentId;
  private final ReservationTerminationId terminationId;
  private final Money amount;
  private final String status;
  private final String idempotencyKey;
  private final Instant recordedAt;
  private final Instant confirmedAt;

  private Refund(
      RefundId id,
      PaymentId paymentId,
      ReservationTerminationId terminationId,
      Money amount,
      String status,
      String idempotencyKey,
      Instant recordedAt,
      Instant confirmedAt) {
    this.id = Objects.requireNonNull(id);
    this.paymentId = Objects.requireNonNull(paymentId);
    this.terminationId = terminationId;
    this.amount = Objects.requireNonNull(amount);
    if (amount.amount().signum() <= 0) throw new BusinessRuleException("Refund must be positive");
    if (!Set.of("PENDING", "CONFIRMED", "FAILED").contains(status))
      throw new BusinessRuleException("Invalid refund status");
    if (("CONFIRMED".equals(status) && confirmedAt == null)
        || (!"CONFIRMED".equals(status) && confirmedAt != null))
      throw new BusinessRuleException("Invalid refund confirmation");
    if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 200)
      throw new BusinessRuleException("Invalid idempotency key");
    this.status = status;
    this.idempotencyKey = idempotencyKey;
    this.recordedAt = Objects.requireNonNull(recordedAt);
    this.confirmedAt = confirmedAt;
  }

  public static Refund pending(
      RefundId id,
      PaymentId paymentId,
      ReservationTerminationId terminationId,
      Money amount,
      String idempotencyKey,
      Instant now) {
    var result =
        new Refund(id, paymentId, terminationId, amount, "PENDING", idempotencyKey, now, null);
    result.recordEvent(new RefundChanged(id, now));
    return result;
  }

  public static Refund restore(
      RefundId id,
      PaymentId paymentId,
      ReservationTerminationId terminationId,
      Money amount,
      String status,
      String idempotencyKey,
      Instant recordedAt,
      Instant confirmedAt) {
    return new Refund(
        id, paymentId, terminationId, amount, status, idempotencyKey, recordedAt, confirmedAt);
  }

  public RefundId id() {
    return id;
  }

  public PaymentId paymentId() {
    return paymentId;
  }

  public ReservationTerminationId terminationId() {
    return terminationId;
  }

  public Money amount() {
    return amount;
  }

  public String status() {
    return status;
  }

  public String idempotencyKey() {
    return idempotencyKey;
  }

  public Instant recordedAt() {
    return recordedAt;
  }

  public Instant confirmedAt() {
    return confirmedAt;
  }
}
