package com.toolrental.infrastructure.unitreturn.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "unit_returns")
public class UnitReturnJpaEntity {
  @EmbeddedId private UnitReturnJpaId id;

  @Column(name = "received_at", nullable = false)
  private Instant receivedAt;

  @Column(name = "inbound_condition", nullable = false, length = 500)
  private String inboundCondition;

  @Column(name = "notes", nullable = true, length = 2000)
  private String notes;

  protected UnitReturnJpaEntity() {}

  public UnitReturnJpaEntity(
      UnitReturnJpaId id, Instant receivedAt, String inboundCondition, String notes) {
    this.id = id;
    this.receivedAt = receivedAt;
    this.inboundCondition = inboundCondition;
    this.notes = notes;
  }

  public UnitReturnJpaId getId() {
    return id;
  }

  public Instant getReceivedAt() {
    return receivedAt;
  }

  public String getInboundCondition() {
    return inboundCondition;
  }

  public String getNotes() {
    return notes;
  }
}
