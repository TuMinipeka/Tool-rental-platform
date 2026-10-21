package com.toolrental.application.category.dto;

import com.toolrental.domain.category.model.aggregate.Category;
import com.toolrental.domain.category.model.valueobject.CategoryId;

public record CategoryResponse(CategoryId id, String name, String description) {
  public static CategoryResponse from(Category value) {
    return new CategoryResponse(value.id(), value.name(), value.description());
  }
}
