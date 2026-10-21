package com.toolrental.domain.tool.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import java.time.Instant;

public record ToolUpdated(ToolId id, Instant occurredOn) implements DomainEvent {}
