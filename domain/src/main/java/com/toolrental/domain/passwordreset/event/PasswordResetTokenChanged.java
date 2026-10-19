package com.toolrental.domain.passwordreset.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.passwordreset.model.valueobject.PasswordResetTokenId;
import java.time.Instant;

public record PasswordResetTokenChanged(PasswordResetTokenId aggregateId, Instant occurredOn)
    implements DomainEvent {}
