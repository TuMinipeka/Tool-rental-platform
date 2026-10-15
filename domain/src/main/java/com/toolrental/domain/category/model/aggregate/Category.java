package com.toolrental.domain.category.model.aggregate;

import com.toolrental.domain.category.event.CategoryRegistered;
import com.toolrental.domain.category.model.valueobject.CategoryId;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.common.model.valueobject.Validation;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class Category extends AggregateRoot {
  private final CategoryId id;
  private final String name;
  private String description;

  private Category(CategoryId id, String name) {
    this.id = Objects.requireNonNull(id);
    this.name = Validation.requiredText(name, "name");
  }

  public static Category register(String name, Clock clock) {
    var category = new Category(CategoryId.generate(), name);
    category.recordEvent(new CategoryRegistered(category.id, Instant.now(clock)));
    return category;
  }

  public static Category restore(CategoryId id, String name) {
    return new Category(id, name);
  }

  public static Category restore(CategoryId id, String name, String description) {
    var result = new Category(id, name); result.description = description; return result;
  }
  public String description() { return description; }
  public CategoryId id() {
    return id;
  }

  public String name() {
    return name;
  }
}
