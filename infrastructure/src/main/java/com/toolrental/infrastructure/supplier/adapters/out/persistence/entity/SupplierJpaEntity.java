package com.toolrental.infrastructure.supplier.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "suppliers")
public class SupplierJpaEntity {
  @Id
  @Column(name = "user_id")
  private UUID id;

  @Column(name = "document_type", nullable = false, length = 30)
  private String documentType;

  @Column(name = "document_number", nullable = false, length = 80)
  private String documentNumber;

  @Column(name = "business_name", nullable = false, length = 200)
  private String businessName;

  protected SupplierJpaEntity() {}

  public SupplierJpaEntity(
      UUID id, String documentType, String documentNumber, String businessName) {
    this.id = id;
    this.documentType = documentType;
    this.documentNumber = documentNumber;
    this.businessName = businessName;
  }

  public UUID getId() {
    return id;
  }

  public String getDocumentType() {
    return documentType;
  }

  public String getDocumentNumber() {
    return documentNumber;
  }

  public String getBusinessName() {
    return businessName;
  }
}
