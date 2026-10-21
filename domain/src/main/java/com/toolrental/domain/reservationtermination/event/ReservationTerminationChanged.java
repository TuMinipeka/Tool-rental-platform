package com.toolrental.domain.reservationtermination.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.reservationtermination.model.valueobject.ReservationTerminationId;
import java.time.Instant;

public record ReservationTerminationChanged(ReservationTerminationId id, Instant occurredOn)
    implements DomainEvent {}
