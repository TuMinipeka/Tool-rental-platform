package com.toolrental.domain.invoiceitem.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.invoiceitem.model.valueobject.InvoiceItemId;
import java.time.Instant;

public record InvoiceItemChanged(InvoiceItemId id, Instant occurredOn) implements DomainEvent {}
