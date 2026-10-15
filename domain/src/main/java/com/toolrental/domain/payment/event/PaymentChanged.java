package com.toolrental.domain.payment.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.payment.model.valueobject.PaymentId;
import java.time.Instant;

public record PaymentChanged(PaymentId id, Instant occurredOn) implements DomainEvent {}
