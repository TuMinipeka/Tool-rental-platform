package com.toolrental.domain.legalacceptance.model.aggregate;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.legalacceptance.model.valueobject.LegalAcceptanceId;
import com.toolrental.domain.user.model.valueobject.UserId;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public final class LegalAcceptance extends AggregateRoot {
  private static final Set<String> DOCUMENT_TYPES =
      Set.of("TERMS_AND_CONDITIONS", "PRIVACY_NOTICE");

  private final LegalAcceptanceId id;
  private final UserId userId;
  private final String documentType;
  private final String documentVersion;
  private final String purpose;
  private final Instant acceptedAt;

  private LegalAcceptance(
      LegalAcceptanceId id,
      UserId userId,
      String documentType,
      String documentVersion,
      String purpose,
      Instant acceptedAt) {
    this.id = Objects.requireNonNull(id);
    this.userId = Objects.requireNonNull(userId);
    this.documentType = normalizeDocumentType(documentType);
    this.documentVersion = validateText(documentVersion, "documentVersion", 40);
    this.purpose = validatePurpose(purpose);
    this.acceptedAt = Objects.requireNonNull(acceptedAt);
  }

  public static LegalAcceptance accept(
      LegalAcceptanceId id,
      UserId userId,
      String documentType,
      String documentVersion,
      String purpose,
      Clock clock) {
    return new LegalAcceptance(
        id, userId, documentType, documentVersion, purpose, Instant.now(clock));
  }

  public static LegalAcceptance restore(
      LegalAcceptanceId id,
      UserId userId,
      String documentType,
      String documentVersion,
      String purpose,
      Instant acceptedAt) {
    return new LegalAcceptance(id, userId, documentType, documentVersion, purpose, acceptedAt);
  }

  private static String normalizeDocumentType(String value) {
    var normalized = validateText(value, "documentType", 50).toUpperCase(Locale.ROOT);
    if (!DOCUMENT_TYPES.contains(normalized))
      throw new BusinessRuleException("Unsupported legal document type");
    return normalized;
  }

  private static String validatePurpose(String value) {
    var normalized = validateText(value, "purpose", 80).toUpperCase(Locale.ROOT);
    if (!normalized.matches("[A-Z0-9_]+"))
      throw new BusinessRuleException("Invalid legal acceptance purpose");
    return normalized;
  }

  private static String validateText(String value, String field, int maxLength) {
    if (value == null || value.isBlank() || value.length() > maxLength)
      throw new BusinessRuleException("Invalid " + field);
    return value.trim();
  }

  public LegalAcceptanceId id() {
    return id;
  }

  public UserId userId() {
    return userId;
  }

  public String documentType() {
    return documentType;
  }

  public String documentVersion() {
    return documentVersion;
  }

  public String purpose() {
    return purpose;
  }

  public Instant acceptedAt() {
    return acceptedAt;
  }
}
