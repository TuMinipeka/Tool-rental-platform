package com.toolrental.infrastructure.legalacceptance.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "legal_acceptances")
public class LegalAcceptanceJpaEntity {
  @Id
  @Column(name = "legal_acceptance_id")
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "document_type", nullable = false, length = 50)
  private String documentType;

  @Column(name = "document_version", nullable = false, length = 40)
  private String documentVersion;

  @Column(name = "purpose", nullable = false, length = 80)
  private String purpose;

  @Column(name = "accepted_at", nullable = false)
  private Instant acceptedAt;

  protected LegalAcceptanceJpaEntity() {}

  public LegalAcceptanceJpaEntity(
      UUID id,
      UUID userId,
      String documentType,
      String documentVersion,
      String purpose,
      Instant acceptedAt) {
    this.id = id;
    this.userId = userId;
    this.documentType = documentType;
    this.documentVersion = documentVersion;
    this.purpose = purpose;
    this.acceptedAt = acceptedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getDocumentType() {
    return documentType;
  }

  public String getDocumentVersion() {
    return documentVersion;
  }

  public String getPurpose() {
    return purpose;
  }

  public Instant getAcceptedAt() {
    return acceptedAt;
  }
}
