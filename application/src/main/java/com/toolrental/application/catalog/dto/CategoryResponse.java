package com.toolrental.application.catalog.dto;

import com.toolrental.domain.catalog.model.aggregate.Category;
import com.toolrental.domain.catalog.model.valueobject.CategoryId;

public record CategoryResponse(CategoryId id, String name) {
  public static CategoryResponse from(Category c) {
    return new CategoryResponse(c.id(), c.name());
  }
}
