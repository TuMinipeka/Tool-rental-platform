package com.toolrental.application.legalacceptance.dto;

import com.toolrental.domain.legalacceptance.model.aggregate.LegalAcceptance;
import java.time.Instant;
import java.util.UUID;

public record LegalAcceptanceResponse(
    UUID id,
    UUID userId,
    String documentType,
    String documentVersion,
    String purpose,
    Instant acceptedAt) {
  public static LegalAcceptanceResponse from(LegalAcceptance value) {
    return new LegalAcceptanceResponse(
        value.id().value(),
        value.userId().value(),
        value.documentType(),
        value.documentVersion(),
        value.purpose(),
        value.acceptedAt());
  }
}
