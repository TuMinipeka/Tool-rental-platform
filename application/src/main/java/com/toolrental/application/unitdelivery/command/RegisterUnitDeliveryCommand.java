package com.toolrental.application.unitdelivery.command;

import com.toolrental.domain.unitdelivery.model.valueobject.UnitDeliveryId;
import java.time.Instant;

public record RegisterUnitDeliveryCommand(
    UnitDeliveryId id,
    Instant scheduledAt,
    String outboundCondition,
    String addressLine,
    String city,
    String region,
    String country) {}
