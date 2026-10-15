package com.toolrental.application.damagereport.command;

import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import java.math.BigDecimal;

public record RegisterDamageReportCommand(
    ReservationId reservationId,
    EquipmentUnitId equipmentUnitId,
    String description,
    BigDecimal assessedCost) {}
