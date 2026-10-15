package com.toolrental.domain.catalog.model.aggregate;

import com.toolrental.domain.catalog.event.ToolRegistered;
import com.toolrental.domain.catalog.model.valueobject.CategoryId;
import com.toolrental.domain.catalog.model.valueobject.ToolId;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.common.model.valueobject.SupplierId;
import com.toolrental.domain.common.model.valueobject.Validation;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public final class Tool extends AggregateRoot {
  private final ToolId id;
  private final SupplierId supplierId;
  private final CategoryId categoryId;
  private final String name;
  private final String description;
  private final Set<URI> imageUrls;
  private Money dailyRate;
  private boolean published;

  private Tool(
      ToolId id,
      SupplierId supplier,
      CategoryId category,
      String name,
      String description,
      Money rate,
      Set<URI> imageUrls,
      boolean published) {
    this.id = Objects.requireNonNull(id);
    this.published = published;
    this.supplierId = Objects.requireNonNull(supplier);
    this.categoryId = Objects.requireNonNull(category);
    this.name = Validation.requiredText(name, "name");
    this.description = Validation.requiredText(description, "description");
    this.dailyRate = Objects.requireNonNull(rate);
    this.imageUrls = Set.copyOf(imageUrls);
    for (URI imagen : this.imageUrls) {
      if (!("https".equalsIgnoreCase(imagen.getScheme())
              || "http".equalsIgnoreCase(imagen.getScheme()))
          || imagen.getHost() == null)
        throw new BusinessRuleException("Tool images require an absolute HTTP(S) URL");
    }
  }

  public static Tool register(
      SupplierId supplier,
      CategoryId category,
      String name,
      String description,
      Money rate,
      Set<URI> imageUrls,
      Clock clock) {
    var tool =
        new Tool(ToolId.generate(), supplier, category, name, description, rate, imageUrls, true);
    tool.recordEvent(new ToolRegistered(tool.id, Instant.now(clock)));
    return tool;
  }

  public static Tool restore(
      ToolId id,
      SupplierId supplier,
      CategoryId category,
      String name,
      String description,
      Money rate,
      Set<URI> imageUrls,
      boolean published) {
    return new Tool(id, supplier, category, name, description, rate, imageUrls, published);
  }

  public void changeDailyRate(Money rate) {
    this.dailyRate = Objects.requireNonNull(rate);
  }

  public void unpublish() {
    published = false;
  }

  public ToolId id() {
    return id;
  }

  public SupplierId supplierId() {
    return supplierId;
  }

  public CategoryId categoryId() {
    return categoryId;
  }

  public String name() {
    return name;
  }

  public String description() {
    return description;
  }

  public Set<URI> imageUrls() {
    return imageUrls;
  }

  public Money dailyRate() {
    return dailyRate;
  }

  public boolean published() {
    return published;
  }
}
