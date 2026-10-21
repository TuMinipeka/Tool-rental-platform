package com.toolrental.domain.supplier.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import java.time.Instant;

public record SupplierChanged(SupplierId id, Instant occurredOn) implements DomainEvent {}
