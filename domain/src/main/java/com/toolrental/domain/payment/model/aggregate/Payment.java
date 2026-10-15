package com.toolrental.domain.payment.model.aggregate;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.payment.event.PaymentChanged;
import com.toolrental.domain.payment.model.valueobject.PaymentId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public final class Payment extends AggregateRoot {
  private final PaymentId id;
  private final ReservationId reservationId;
  private final String processor;
  private final String externalReference;
  private final BigDecimal amount;
  private final String paymentStatus;
  private final Instant recordedAt;
  private final Instant confirmedAt;

  private Payment(
      PaymentId id,
      ReservationId reservationId,
      String processor,
      String externalReference,
      BigDecimal amount,
      String paymentStatus,
      Instant recordedAt,
      Instant confirmedAt) {
    Objects.requireNonNull(id);
    Objects.requireNonNull(reservationId, "reservationId is required");
    Objects.requireNonNull(processor, "processor is required");
    if (processor != null && (processor.isBlank() || processor.length() > 80))
      throw new BusinessRuleException("Invalid processor");
    Objects.requireNonNull(externalReference, "externalReference is required");
    if (externalReference != null
        && (externalReference.isBlank() || externalReference.length() > 200))
      throw new BusinessRuleException("Invalid externalReference");
    Objects.requireNonNull(amount, "amount is required");
    if (amount != null && (amount.signum() <= 0 || amount.scale() > 4 || amount.precision() > 19))
      throw new BusinessRuleException("Invalid amount");
    Objects.requireNonNull(paymentStatus, "paymentStatus is required");
    if (paymentStatus != null && (paymentStatus.isBlank() || paymentStatus.length() > 20))
      throw new BusinessRuleException("Invalid paymentStatus");
    if (!Set.of("PENDING", "CONFIRMED", "FAILED", "REFUNDED").contains(paymentStatus))
      throw new BusinessRuleException("Invalid paymentStatus");
    Objects.requireNonNull(recordedAt, "recordedAt is required");
    if (("CONFIRMED".equals(paymentStatus) || "REFUNDED".equals(paymentStatus))
        && confirmedAt == null)
      throw new BusinessRuleException("Confirmed payment requires confirmation time");
    this.id = id;
    this.reservationId = reservationId;
    this.processor = processor;
    this.externalReference = externalReference;
    this.amount = amount;
    this.paymentStatus = paymentStatus;
    this.recordedAt = recordedAt;
    this.confirmedAt = confirmedAt;
  }

  public static Payment restore(
      PaymentId id,
      ReservationId reservationId,
      String processor,
      String externalReference,
      BigDecimal amount,
      String paymentStatus,
      Instant recordedAt,
      Instant confirmedAt) {
    return new Payment(
        id,
        reservationId,
        processor,
        externalReference,
        amount,
        paymentStatus,
        recordedAt,
        confirmedAt);
  }

  public static Payment register(
      PaymentId id,
      ReservationId reservationId,
      String processor,
      String externalReference,
      BigDecimal amount,
      String paymentStatus,
      Instant recordedAt,
      Instant confirmedAt,
      Clock clock) {
    var result =
        new Payment(
            id,
            reservationId,
            processor,
            externalReference,
            amount,
            paymentStatus,
            recordedAt,
            confirmedAt);
    result.recordEvent(new PaymentChanged(id, Instant.now(clock)));
    return result;
  }

  public PaymentId id() {
    return id;
  }

  public ReservationId reservationId() {
    return reservationId;
  }

  public String processor() {
    return processor;
  }

  public String externalReference() {
    return externalReference;
  }

  public BigDecimal amount() {
    return amount;
  }

  public String paymentStatus() {
    return paymentStatus;
  }

  public Instant recordedAt() {
    return recordedAt;
  }

  public Instant confirmedAt() {
    return confirmedAt;
  }

  public Payment transition(String next, Clock clock) {
    if (!(("PENDING".equals(paymentStatus) && Set.of("CONFIRMED", "FAILED").contains(next))
        || ("CONFIRMED".equals(paymentStatus) && Set.of("REFUNDED").contains(next))))
      throw new BusinessRuleException("Invalid Payment transition");
    var result =
        new Payment(
            id,
            reservationId,
            processor,
            externalReference,
            amount,
            next,
            recordedAt,
            ("CONFIRMED".equals(next) ? Instant.now(clock) : confirmedAt));
    result.recordEvent(new PaymentChanged(id, Instant.now(clock)));
    return result;
  }
}
