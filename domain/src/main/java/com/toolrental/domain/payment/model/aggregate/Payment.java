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
  private final String paymentMethod;
  private final String paymentStage;
  private final String idempotencyKey;
  private final String cardNetwork;
  private final String cardLastFour;
  private final Short cardExpiryMonth;
  private final Short cardExpiryYear;
  private final String adjustmentType;
  private final java.util.UUID adjustmentReference;

  private Payment(
      PaymentId id,
      ReservationId reservationId,
      String processor,
      String externalReference,
      BigDecimal amount,
      String paymentStatus,
      Instant recordedAt,
      Instant confirmedAt,
      String paymentMethod,
      String paymentStage,
      String idempotencyKey,
      String cardNetwork,
      String cardLastFour,
      Short cardExpiryMonth,
      Short cardExpiryYear,
      String adjustmentType,
      java.util.UUID adjustmentReference) {
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
    if (!Set.of("ADMINISTRATIVE", "CREDIT_CARD", "DEBIT_CARD", "PAYPAL").contains(paymentMethod))
      throw new BusinessRuleException("Invalid payment method");
    if (!Set.of("ADMINISTRATIVE", "FIRST_HALF", "FINAL_HALF", "FULL_PAYMENT", "ADJUSTMENT")
        .contains(paymentStage)) throw new BusinessRuleException("Invalid payment stage");
    if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 200)
      throw new BusinessRuleException("Invalid idempotency key");
    var cardPayment = Set.of("CREDIT_CARD", "DEBIT_CARD").contains(paymentMethod);
    if (cardPayment) {
      if (!Set.of("VISA", "MASTERCARD").contains(cardNetwork)
          || cardLastFour == null
          || !cardLastFour.matches("[0-9]{4}")
          || cardExpiryMonth == null
          || cardExpiryMonth < 1
          || cardExpiryMonth > 12
          || cardExpiryYear == null)
        throw new BusinessRuleException("Invalid simulated card details");
    } else if (cardNetwork != null
        || cardLastFour != null
        || cardExpiryMonth != null
        || cardExpiryYear != null) {
      throw new BusinessRuleException("Non-card payment cannot include card details");
    }
    this.id = id;
    this.reservationId = reservationId;
    this.processor = processor;
    this.externalReference = externalReference;
    this.amount = amount;
    this.paymentStatus = paymentStatus;
    this.recordedAt = recordedAt;
    this.confirmedAt = confirmedAt;
    this.paymentMethod = paymentMethod;
    this.paymentStage = paymentStage;
    this.idempotencyKey = idempotencyKey;
    this.cardNetwork = cardNetwork;
    this.cardLastFour = cardLastFour;
    this.cardExpiryMonth = cardExpiryMonth;
    this.cardExpiryYear = cardExpiryYear;
    if ("ADJUSTMENT".equals(paymentStage)) {
      if (adjustmentType == null || adjustmentType.isBlank() || adjustmentReference == null)
        throw new BusinessRuleException("Adjustment payment requires a source");
    } else if (adjustmentType != null || adjustmentReference != null) {
      throw new BusinessRuleException("Non-adjustment payment cannot have an adjustment source");
    }
    this.adjustmentType = adjustmentType;
    this.adjustmentReference = adjustmentReference;
  }

  public static Payment restore(
      PaymentId id,
      ReservationId reservationId,
      String processor,
      String externalReference,
      BigDecimal amount,
      String paymentStatus,
      Instant recordedAt,
      Instant confirmedAt,
      String paymentMethod,
      String paymentStage,
      String idempotencyKey,
      String cardNetwork,
      String cardLastFour,
      Short cardExpiryMonth,
      Short cardExpiryYear) {
    return restore(
        id,
        reservationId,
        processor,
        externalReference,
        amount,
        paymentStatus,
        recordedAt,
        confirmedAt,
        paymentMethod,
        paymentStage,
        idempotencyKey,
        cardNetwork,
        cardLastFour,
        cardExpiryMonth,
        cardExpiryYear,
        null,
        null);
  }

  public static Payment restore(
      PaymentId id,
      ReservationId reservationId,
      String processor,
      String externalReference,
      BigDecimal amount,
      String paymentStatus,
      Instant recordedAt,
      Instant confirmedAt,
      String paymentMethod,
      String paymentStage,
      String idempotencyKey,
      String cardNetwork,
      String cardLastFour,
      Short cardExpiryMonth,
      Short cardExpiryYear,
      String adjustmentType,
      java.util.UUID adjustmentReference) {
    return new Payment(
        id,
        reservationId,
        processor,
        externalReference,
        amount,
        paymentStatus,
        recordedAt,
        confirmedAt,
        paymentMethod,
        paymentStage,
        idempotencyKey,
        cardNetwork,
        cardLastFour,
        cardExpiryMonth,
        cardExpiryYear,
        adjustmentType,
        adjustmentReference);
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
            confirmedAt,
            "ADMINISTRATIVE",
            "ADMINISTRATIVE",
            processor + ":" + externalReference,
            null,
            null,
            null,
            null,
            null,
            null);
    result.recordEvent(new PaymentChanged(id, Instant.now(clock)));
    return result;
  }

  public static Payment simulate(
      PaymentId id,
      ReservationId reservationId,
      String processor,
      String externalReference,
      BigDecimal amount,
      String paymentStatus,
      Instant recordedAt,
      String paymentMethod,
      String paymentStage,
      String idempotencyKey,
      String cardNetwork,
      String cardLastFour,
      Short cardExpiryMonth,
      Short cardExpiryYear,
      Clock clock) {
    var confirmedAt = "CONFIRMED".equals(paymentStatus) ? Instant.now(clock) : null;
    var result =
        new Payment(
            id,
            reservationId,
            processor,
            externalReference,
            amount,
            paymentStatus,
            recordedAt,
            confirmedAt,
            paymentMethod,
            paymentStage,
            idempotencyKey,
            cardNetwork,
            cardLastFour,
            cardExpiryMonth,
            cardExpiryYear,
            null,
            null);
    result.recordEvent(new PaymentChanged(id, Instant.now(clock)));
    return result;
  }

  public static Payment sandboxCheckout(
      PaymentId id,
      ReservationId reservationId,
      String externalReference,
      BigDecimal amount,
      Instant recordedAt,
      String paymentMethod,
      String idempotencyKey,
      String cardNetwork,
      String cardLastFour,
      Short cardExpiryMonth,
      Short cardExpiryYear,
      Clock clock) {
    var result =
        new Payment(
            id,
            reservationId,
            "ACADEMIC_SANDBOX",
            externalReference,
            amount,
            "PENDING",
            recordedAt,
            null,
            paymentMethod,
            "FULL_PAYMENT",
            idempotencyKey,
            cardNetwork,
            cardLastFour,
            cardExpiryMonth,
            cardExpiryYear,
            null,
            null);
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

  public String paymentMethod() {
    return paymentMethod;
  }

  public String paymentStage() {
    return paymentStage;
  }

  public String idempotencyKey() {
    return idempotencyKey;
  }

  public String cardNetwork() {
    return cardNetwork;
  }

  public String cardLastFour() {
    return cardLastFour;
  }

  public Short cardExpiryMonth() {
    return cardExpiryMonth;
  }

  public Short cardExpiryYear() {
    return cardExpiryYear;
  }

  public String adjustmentType() {
    return adjustmentType;
  }

  public java.util.UUID adjustmentReference() {
    return adjustmentReference;
  }

  public static Payment adjustment(
      PaymentId id,
      ReservationId reservationId,
      BigDecimal amount,
      String adjustmentType,
      java.util.UUID adjustmentReference,
      String idempotencyKey,
      Clock clock) {
    var now = Instant.now(clock);
    var result =
        new Payment(
            id,
            reservationId,
            "INTERNAL_ADJUSTMENT",
            adjustmentReference.toString(),
            amount,
            "PENDING",
            now,
            null,
            "ADMINISTRATIVE",
            "ADJUSTMENT",
            idempotencyKey,
            null,
            null,
            null,
            null,
            adjustmentType,
            adjustmentReference);
    result.recordEvent(new PaymentChanged(id, now));
    return result;
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
            ("CONFIRMED".equals(next) ? Instant.now(clock) : confirmedAt),
            paymentMethod,
            paymentStage,
            idempotencyKey,
            cardNetwork,
            cardLastFour,
            cardExpiryMonth,
            cardExpiryYear,
            adjustmentType,
            adjustmentReference);
    result.recordEvent(new PaymentChanged(id, Instant.now(clock)));
    return result;
  }
}
