package com.toolrental.application.tool.dto;

import com.toolrental.domain.tool.model.aggregate.Tool;
import com.toolrental.domain.category.model.valueobject.CategoryId;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.common.model.valueobject.SupplierId;
import java.net.URI;
import java.util.Set;

public record ToolResponse(
    ToolId id,
    SupplierId supplierId,
    CategoryId categoryId,
    String name,
    String description,
    Money dailyRate,
    Set<URI> imageUrls,
    boolean published) {
  public ToolResponse {
    imageUrls = Set.copyOf(imageUrls);
  }

  public static ToolResponse from(Tool c) {
    return new ToolResponse(
        c.id(),
        c.supplierId(),
        c.categoryId(),
        c.name(),
        c.description(),
        c.dailyRate(),
        c.imageUrls(),
        c.published());
  }
}
