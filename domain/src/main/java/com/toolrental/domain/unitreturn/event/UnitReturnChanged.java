package com.toolrental.domain.unitreturn.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.unitreturn.model.valueobject.UnitReturnId;
import java.time.Instant;

public record UnitReturnChanged(UnitReturnId id, Instant occurredOn) implements DomainEvent {}
