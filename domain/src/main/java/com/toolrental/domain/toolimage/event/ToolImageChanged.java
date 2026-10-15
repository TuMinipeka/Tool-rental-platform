package com.toolrental.domain.toolimage.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.toolimage.model.valueobject.ToolImageId;
import java.time.Instant;

public record ToolImageChanged(ToolImageId id, Instant occurredOn) implements DomainEvent {}
