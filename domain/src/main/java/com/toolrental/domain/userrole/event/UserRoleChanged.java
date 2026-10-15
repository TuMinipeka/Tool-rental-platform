package com.toolrental.domain.userrole.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.userrole.model.valueobject.UserRoleId;
import java.time.Instant;

public record UserRoleChanged(UserRoleId id, Instant occurredOn) implements DomainEvent {}
