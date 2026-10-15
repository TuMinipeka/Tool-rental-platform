package com.toolrental.domain.unitdelivery.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.unitdelivery.model.valueobject.UnitDeliveryId;
import java.time.Instant;

public record UnitDeliveryChanged(UnitDeliveryId id, Instant occurredOn) implements DomainEvent {}
