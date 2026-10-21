package com.toolrental.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.toolrental.domain.cancellationpolicy.model.aggregate.CancellationPolicy;
import com.toolrental.domain.cancellationpolicy.model.valueobject.CancellationPolicyId;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.passwordreset.model.aggregate.PasswordResetToken;
import com.toolrental.domain.passwordreset.model.valueobject.PasswordResetTokenId;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.domain.usersession.model.aggregate.UserSession;
import com.toolrental.domain.usersession.model.valueobject.UserSessionId;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CriticalPolicyTest {
  @Test
  void cancellationCutoffAndNoShowGraceUseExactBoundaries() {
    var policy =
        CancellationPolicy.register(
            CancellationPolicyId.generate(),
            1,
            48,
            new BigDecimal("0.25"),
            new BigDecimal("0.50"),
            120,
            Instant.parse("2026-01-01T00:00:00Z"));
    var fulfillment = Instant.parse("2026-02-03T15:00:00Z");
    assertTrue(policy.isFreeCancellation(fulfillment, fulfillment.minus(Duration.ofHours(48))));
    assertFalse(
        policy.isFreeCancellation(
            fulfillment, fulfillment.minus(Duration.ofHours(48)).plusMillis(1)));
    assertEquals(fulfillment.plus(Duration.ofHours(2)), policy.noShowEligibleAt(fulfillment));
  }

  @Test
  void refreshRotationKeepsFamilyAndDetectsReuseBeyondGrace() {
    var now = Instant.parse("2026-01-01T00:00:00Z");
    var family = UUID.randomUUID();
    var session =
        UserSession.start(
            UserSessionId.generate(),
            family,
            new UserId(UUID.randomUUID()),
            "a".repeat(64),
            now,
            now.plus(Duration.ofDays(30)),
            "browser");
    var rotation =
        session.rotate(
            UserSessionId.generate(),
            "b".repeat(64),
            now.plusSeconds(1),
            now.plus(Duration.ofDays(30)));
    assertEquals(family, rotation.successor().familyId());
    assertFalse(
        rotation.rotated().isRotatedBeyondGrace(now.plusSeconds(10), Duration.ofSeconds(10)));
    assertTrue(
        rotation.rotated().isRotatedBeyondGrace(now.plusSeconds(11), Duration.ofSeconds(10)));
  }

  @Test
  void percentageMoneyUsesFourDecimalSettlementPrecision() {
    var total = new Money(new BigDecimal("100001"), Currency.getInstance("COP"));
    assertEquals(
        0, total.multiply(new BigDecimal("0.25")).amount().compareTo(new BigDecimal("25000.2500")));
    assertThrows(RuntimeException.class, () -> total.multiply(new BigDecimal("-0.01")));
  }

  @Test
  void passwordResetTokenIsSingleUseAndExpiresAtTheExactBoundary() {
    var issuedAt = Instant.parse("2026-01-01T00:00:00Z");
    var expiresAt = issuedAt.plus(Duration.ofMinutes(15));
    var token =
        PasswordResetToken.issue(
            PasswordResetTokenId.generate(),
            new UserId(UUID.randomUUID()),
            "c".repeat(64),
            issuedAt,
            expiresAt);

    var consumed = token.consume(expiresAt.minusMillis(1));
    assertNotNull(consumed.usedAt());
    assertThrows(RuntimeException.class, () -> consumed.consume(expiresAt.minusMillis(1)));
    assertThrows(RuntimeException.class, () -> token.consume(expiresAt));
  }
}
