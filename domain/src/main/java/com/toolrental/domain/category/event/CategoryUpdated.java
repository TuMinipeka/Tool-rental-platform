package com.toolrental.domain.category.event;

import com.toolrental.domain.category.model.valueobject.CategoryId;
import com.toolrental.domain.common.event.DomainEvent;
import java.time.Instant;

public record CategoryUpdated(CategoryId id, Instant occurredOn) implements DomainEvent {}
