package com.toolrental.domain.reservationextension.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.reservationextension.model.valueobject.ReservationExtensionId;
import java.time.Instant;

public record ReservationExtensionChanged(ReservationExtensionId aggregateId, Instant occurredOn)
    implements DomainEvent {}
