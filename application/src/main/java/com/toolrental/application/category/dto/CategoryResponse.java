package com.toolrental.application.category.dto;

import com.toolrental.domain.category.model.aggregate.Category;
import com.toolrental.domain.category.model.valueobject.CategoryId;

public record CategoryResponse(CategoryId id, String name) {
  public static CategoryResponse from(Category c) {
    return new CategoryResponse(c.id(), c.name());
  }
}
