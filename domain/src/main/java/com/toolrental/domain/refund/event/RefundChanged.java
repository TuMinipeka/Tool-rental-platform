package com.toolrental.domain.refund.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.refund.model.valueobject.RefundId;
import java.time.Instant;

public record RefundChanged(RefundId aggregateId, Instant occurredOn) implements DomainEvent {}
