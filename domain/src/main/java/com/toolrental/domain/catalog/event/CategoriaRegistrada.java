package com.toolrental.domain.catalog.event;

import com.toolrental.domain.catalog.model.valueobject.CategoriaId;
import com.toolrental.domain.common.event.DomainEvent;
import java.time.Instant;
import java.util.Objects;

public record CategoriaRegistrada(CategoriaId id, Instant occurredOn) implements DomainEvent {
  public CategoriaRegistrada {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(occurredOn, "occurredOn must not be null");
  }
}
