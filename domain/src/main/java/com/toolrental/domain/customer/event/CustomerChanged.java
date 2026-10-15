package com.toolrental.domain.customer.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.customer.model.valueobject.CustomerId;
import java.time.Instant;

public record CustomerChanged(CustomerId id, Instant occurredOn) implements DomainEvent {}
