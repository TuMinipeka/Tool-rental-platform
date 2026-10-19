package com.toolrental.domain.cancellationpolicy.event;

import com.toolrental.domain.cancellationpolicy.model.valueobject.CancellationPolicyId;
import com.toolrental.domain.common.event.DomainEvent;
import java.time.Instant;

public record CancellationPolicyChanged(CancellationPolicyId id, Instant occurredOn)
    implements DomainEvent {}
