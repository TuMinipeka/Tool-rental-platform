package com.toolrental.domain.usersession.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.usersession.model.valueobject.UserSessionId;
import java.time.Instant;

public record UserSessionChanged(UserSessionId aggregateId, Instant occurredOn)
    implements DomainEvent {}
