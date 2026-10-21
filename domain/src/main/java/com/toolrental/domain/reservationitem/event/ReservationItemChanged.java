package com.toolrental.domain.reservationitem.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.reservationitem.model.valueobject.ReservationItemId;
import java.time.Instant;

public record ReservationItemChanged(ReservationItemId id, Instant occurredOn)
    implements DomainEvent {}
