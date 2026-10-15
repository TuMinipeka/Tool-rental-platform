package com.toolrental.infrastructure.tool.adapters.out.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tools")
public class ToolJpaEntity {
  @Id
  @Column(name = "tool_id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "supplier_id", nullable = false, updatable = false)
  private UUID supplierId;

  @Column(name = "category_id", nullable = false)
  private UUID categoryId;

  @Column(name = "name", nullable = false, length = 160)
  private String name;

  @Column(name = "description", nullable = false, length = 2000)
  private String description;

  @Column(name = "current_daily_rate", nullable = false, precision = 19, scale = 4)
  private BigDecimal currentDailyRate;

  @Column(name = "rate_currency", nullable = false, length = 3)
  private String rateCurrency;

  @Column(name = "publication_status", nullable = false, length = 20)
  private String publicationStatus;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "tool_images", joinColumns = @JoinColumn(name = "tool_id"))
  @Column(name = "image_url", nullable = false, length = 2048)
  private Set<String> imageUrls = new LinkedHashSet<>();

  protected ToolJpaEntity() {}

  public ToolJpaEntity(
      UUID id,
      UUID supplierId,
      UUID categoryId,
      String name,
      String description,
      BigDecimal currentDailyRate,
      String rateCurrency,
      String publicationStatus,
      Set<String> imageUrls) {
    this.id = id;
    this.supplierId = supplierId;
    this.categoryId = categoryId;
    this.name = name;
    this.description = description;
    this.currentDailyRate = currentDailyRate;
    this.rateCurrency = rateCurrency;
    this.publicationStatus = publicationStatus;
    this.imageUrls = new LinkedHashSet<>(imageUrls);
  }

  public UUID getId() {
    return id;
  }

  public UUID getSupplierId() {
    return supplierId;
  }

  public UUID getCategoryId() {
    return categoryId;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public BigDecimal getCurrentDailyRate() {
    return currentDailyRate;
  }

  public String getRateCurrency() {
    return rateCurrency;
  }

  public String getPublicationStatus() {
    return publicationStatus;
  }

  public Set<String> getImageUrls() {
    return Set.copyOf(imageUrls);
  }
}
