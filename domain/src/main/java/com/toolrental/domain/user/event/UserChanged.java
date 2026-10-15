package com.toolrental.domain.user.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.user.model.valueobject.UserId;
import java.time.Instant;

public record UserChanged(UserId id, Instant occurredOn) implements DomainEvent {}
