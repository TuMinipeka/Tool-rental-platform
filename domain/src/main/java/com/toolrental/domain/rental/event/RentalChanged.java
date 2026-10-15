package com.toolrental.domain.rental.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.rental.model.valueobject.RentalId;
import java.time.Instant;

public record RentalChanged(RentalId id, Instant occurredOn) implements DomainEvent {}
