package com.toolrental.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.payment.model.aggregate.Payment;
import com.toolrental.domain.payment.model.valueobject.PaymentId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.user.model.aggregate.User;
import com.toolrental.domain.user.model.valueobject.UserId;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OperationalRecordsTest {
  final Clock clock = Clock.fixed(Instant.parse("2026-10-15T12:00:00Z"), ZoneOffset.UTC);

  @Test
  void restoreDoesNotEmitEventsAndDisabledUserIsTerminal() {
    var user =
        User.restore(
            new UserId(UUID.randomUUID()),
            "test@example.com",
            "Name",
            "HASH",
            "ACTIVE",
            clock.instant());
    assertTrue(user.domainEvents().isEmpty());
    var disabled = user.transition("DISABLED", clock);
    assertEquals(1, disabled.domainEvents().size());
    assertThrows(BusinessRuleException.class, () -> disabled.transition("ACTIVE", clock));
  }

  @Test
  void paymentsRejectZeroAndPreserveConfirmationOnRefund() {
    var id = PaymentId.generate();
    var reservation = ReservationId.generate();
    assertThrows(
        BusinessRuleException.class,
        () ->
            Payment.register(
                id,
                reservation,
                "MANUAL",
                "REFERENCE",
                BigDecimal.ZERO,
                "PENDING",
                clock.instant(),
                null,
                clock));
    var pending =
        Payment.register(
            id,
            reservation,
            "MANUAL",
            "REFERENCE",
            BigDecimal.TEN,
            "PENDING",
            clock.instant(),
            null,
            clock);
    assertThrows(BusinessRuleException.class, () -> pending.transition("REFUNDED", clock));
    var confirmed = pending.transition("CONFIRMED", clock);
    var refunded = confirmed.transition("REFUNDED", Clock.offset(clock, Duration.ofDays(1)));
    assertEquals(confirmed.confirmedAt(), refunded.confirmedAt());
    assertThrows(BusinessRuleException.class, () -> refunded.transition("CONFIRMED", clock));
  }

  @Test
  void simulatedCardPaymentStoresOnlyApprovedMetadata() {
    var payment =
        Payment.simulate(
            PaymentId.generate(),
            ReservationId.generate(),
            "SIMULATED_CARD",
            "sim-reference",
            BigDecimal.TEN,
            "CONFIRMED",
            clock.instant(),
            "CREDIT_CARD",
            "FIRST_HALF",
            "checkout-1",
            "VISA",
            "4242",
            (short) 12,
            (short) 2030,
            clock);

    assertEquals("4242", payment.cardLastFour());
    assertEquals("VISA", payment.cardNetwork());
    assertEquals("CONFIRMED", payment.paymentStatus());
    assertThrows(
        BusinessRuleException.class,
        () ->
            Payment.simulate(
                PaymentId.generate(),
                ReservationId.generate(),
                "SIMULATED_PAYPAL",
                "sim-paypal",
                BigDecimal.TEN,
                "CONFIRMED",
                clock.instant(),
                "PAYPAL",
                "FIRST_HALF",
                "checkout-2",
                "MASTERCARD",
                "1111",
                (short) 1,
                (short) 2030,
                clock));
  }
}
