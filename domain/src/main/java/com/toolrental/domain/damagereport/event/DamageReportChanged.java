package com.toolrental.domain.damagereport.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.damagereport.model.valueobject.DamageReportId;
import java.time.Instant;

public record DamageReportChanged(DamageReportId id, Instant occurredOn) implements DomainEvent {}
