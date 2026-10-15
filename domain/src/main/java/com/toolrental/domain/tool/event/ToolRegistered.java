package com.toolrental.domain.tool.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import java.time.Instant;
import java.util.Objects;

public record ToolRegistered(ToolId id, Instant occurredOn) implements DomainEvent {
  public ToolRegistered {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(occurredOn, "occurredOn must not be null");
  }
}
