package com.toolrental.domain.role.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.role.model.valueobject.RoleId;
import java.time.Instant;

public record RoleChanged(RoleId id, Instant occurredOn) implements DomainEvent {}
