package com.toolrental.domain.category.event;

import com.toolrental.domain.category.model.valueobject.CategoryId;
import com.toolrental.domain.common.event.DomainEvent;
import java.time.Instant;
import java.util.Objects;

public record CategoryRegistered(CategoryId id, Instant occurredOn) implements DomainEvent {
  public CategoryRegistered {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(occurredOn, "occurredOn must not be null");
  }
}
