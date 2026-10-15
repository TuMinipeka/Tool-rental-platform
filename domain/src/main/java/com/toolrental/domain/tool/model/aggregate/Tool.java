package com.toolrental.domain.tool.model.aggregate;

import com.toolrental.domain.category.model.valueobject.CategoryId;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.common.model.valueobject.Validation;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import com.toolrental.domain.tool.event.ToolRegistered;
import com.toolrental.domain.tool.model.valueobject.ToolId;
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
    if (this.name.length() > 160 || this.description.length() > 2000)
      throw new BusinessRuleException("Tool text exceeds the supported length");
    this.dailyRate = Objects.requireNonNull(rate);
    this.imageUrls = Set.copyOf(imageUrls);
    for (URI image : this.imageUrls) {
      if (image.toString().length() > 2048)
        throw new BusinessRuleException("Image URL is too long");
      if (!("https".equalsIgnoreCase(image.getScheme())
              || "http".equalsIgnoreCase(image.getScheme()))
          || image.getHost() == null)
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
    if (this.name.length() > 160 || this.description.length() > 2000)
      throw new BusinessRuleException("Tool text exceeds the supported length");
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

  public Tool update(String name, String description, Money rate, boolean published, Clock clock) {
    var changed =
        new Tool(id, supplierId, categoryId, name, description, rate, imageUrls, published);
    changed.recordEvent(new com.toolrental.domain.tool.event.ToolUpdated(id, Instant.now(clock)));
    return changed;
  }
}
