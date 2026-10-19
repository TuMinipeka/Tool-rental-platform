package com.toolrental.domain.deliveryrate.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.deliveryrate.model.valueobject.DeliveryRateId;
import java.time.Instant;

public record DeliveryRateChanged(DeliveryRateId id, Instant occurredOn) implements DomainEvent {}
