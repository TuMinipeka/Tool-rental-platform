package com.toolrental.application.tool.dto;

import com.toolrental.domain.tool.model.aggregate.Tool;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record CatalogToolResponse(
    UUID toolId,
    UUID supplierId,
    UUID categoryId,
    String categoryName,
    String name,
    String description,
    String serviceCity,
    BigDecimal dailyRate,
    String currency,
    List<URI> imageUrls,
    long operationalUnitCount) {
  public CatalogToolResponse {
    imageUrls = List.copyOf(imageUrls);
  }

  public static CatalogToolResponse from(
      Tool tool, String categoryName, long operationalUnitCount) {
    return new CatalogToolResponse(
        tool.id().value(),
        tool.supplierId().value(),
        tool.categoryId().value(),
        categoryName,
        tool.name(),
        tool.description(),
        tool.serviceCity(),
        tool.dailyRate().amount(),
        tool.dailyRate().currency().getCurrencyCode(),
        tool.imageUrls().stream().sorted(Comparator.comparing(URI::toString)).toList(),
        operationalUnitCount);
  }
}
