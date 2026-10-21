package com.toolrental.domain.equipmentunit.event;

import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import java.time.Instant;

public record EquipmentUnitUpdated(EquipmentUnitId id, Instant occurredOn) implements DomainEvent {}
