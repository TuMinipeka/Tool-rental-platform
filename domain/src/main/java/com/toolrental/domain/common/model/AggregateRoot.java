package com.toolrental.domain.common.model;

import com.toolrental.domain.common.event.DomainEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AggregateRoot {
  private final List<DomainEvent> domainEvents = new ArrayList<>();

  protected final void recordEvent(DomainEvent event) {
    domainEvents.add(Objects.requireNonNull(event, "event must not be null"));
  }

  public final List<DomainEvent> domainEvents() {
    return List.copyOf(domainEvents);
  }

  public final void clearDomainEvents() {
    domainEvents.clear();
  }
}
