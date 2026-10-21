package com.toolrental.infrastructure.customer.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class CustomerJpaEntity {
  @Id
  @Column(name = "user_id")
  private UUID id;

  @Column(name = "person_type", nullable = false, length = 20)
  private String personType;

  @Column(name = "document_type", nullable = false, length = 30)
  private String documentType;

  @Column(name = "document_number", nullable = false, length = 80)
  private String documentNumber;

  @Column(name = "billing_name", nullable = false, length = 200)
  private String billingName;

  protected CustomerJpaEntity() {}

  public CustomerJpaEntity(
      UUID id, String personType, String documentType, String documentNumber, String billingName) {
    this.id = id;
    this.personType = personType;
    this.documentType = documentType;
    this.documentNumber = documentNumber;
    this.billingName = billingName;
  }

  public UUID getId() {
    return id;
  }

  public String getPersonType() {
    return personType;
  }

  public String getDocumentType() {
    return documentType;
  }

  public String getDocumentNumber() {
    return documentNumber;
  }

  public String getBillingName() {
    return billingName;
  }
}
