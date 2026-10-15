package com.toolrental.domain.invoice.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.invoice.model.valueobject.InvoiceId;
import java.time.Instant;

public record InvoiceChanged(InvoiceId id, Instant occurredOn) implements DomainEvent {}
