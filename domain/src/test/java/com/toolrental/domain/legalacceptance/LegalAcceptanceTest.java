package com.toolrental.domain.legalacceptance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.legalacceptance.model.aggregate.LegalAcceptance;
import com.toolrental.domain.legalacceptance.model.valueobject.LegalAcceptanceId;
import com.toolrental.domain.user.model.valueobject.UserId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LegalAcceptanceTest {
  private final Clock clock = Clock.fixed(Instant.parse("2026-10-22T15:00:00Z"), ZoneOffset.UTC);

  @Test
  void normalizesAndFreezesAnAcceptedDocument() {
    var acceptance =
        LegalAcceptance.accept(
            LegalAcceptanceId.generate(),
            new UserId(UUID.randomUUID()),
            "terms_and_conditions",
            "2026-10-20",
            "customer_registration",
            clock);

    assertEquals("TERMS_AND_CONDITIONS", acceptance.documentType());
    assertEquals("CUSTOMER_REGISTRATION", acceptance.purpose());
    assertEquals(Instant.parse("2026-10-22T15:00:00Z"), acceptance.acceptedAt());
  }

  @Test
  void rejectsUnknownDocumentsAndInvalidPurposes() {
    var user = new UserId(UUID.randomUUID());
    assertThrows(
        BusinessRuleException.class,
        () ->
            LegalAcceptance.accept(
                LegalAcceptanceId.generate(),
                user,
                "UNPUBLISHED_DOCUMENT",
                "v1",
                "CHECKOUT",
                clock));
    assertThrows(
        BusinessRuleException.class,
        () ->
            LegalAcceptance.accept(
                LegalAcceptanceId.generate(),
                user,
                "PRIVACY_NOTICE",
                "v1",
                "invalid purpose",
                clock));
  }
}
