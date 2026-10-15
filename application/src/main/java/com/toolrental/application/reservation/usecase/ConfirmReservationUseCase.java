package com.toolrental.application.reservation.usecase;

import com.toolrental.application.reservation.dto.ReservationResponse;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservation.port.repository.AtomicReservationManagement;
import com.toolrental.domain.reservationitem.model.entity.ReservationLine;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import java.time.Clock;
import java.util.Objects;

public final class ConfirmReservationUseCase {
  private final AtomicReservationManagement management;
  private final Clock clock;

  public ConfirmReservationUseCase(AtomicReservationManagement management, Clock clock) {
    this.management = Objects.requireNonNull(management);
    this.clock = Objects.requireNonNull(clock);
  }

  public ReservationResponse execute(ReservationId id, SupplierId actor) {
    Objects.requireNonNull(actor);
    return ReservationResponse.from(
        management.execute(
            Objects.requireNonNull(id),
            context -> {
              var r = context.reservation();
              if (!r.supplierId().equals(actor) || !context.isSupplierActive(actor))
                throw new com.toolrental.application.common.exception
                    .AccessDeniedApplicationException();
              if (!context.isCustomerActive(r.customerId()))
                throw new BusinessRuleException("Customer is inactive");
              var currentItems =
                  r.items().stream()
                      .map(
                          item -> {
                            var equipmentUnit = context.equipmentUnit(item.equipmentUnitId());
                            var tool = context.tool(equipmentUnit.toolId());
                            if (!equipmentUnit.isOperational() || !tool.published())
                              throw new BusinessRuleException("Equipment unit is not rentable");
                            if (!tool.supplierId().equals(actor))
                              throw new BusinessRuleException("Equipment supplier changed");
                            if (!tool.dailyRate().currency().equals(item.dailyRate().currency()))
                              throw new BusinessRuleException(
                                  "Currency changed; request a new reservation");
                            if (context.isOccupied(equipmentUnit.id(), r.period()))
                              throw new BusinessRuleException("Equipment unit is unavailable");
                            return new ReservationLine(equipmentUnit.id(), actor, tool.dailyRate());
                          })
                      .toList();
              return r.confirmWithRates(currentItems, clock);
            }));
  }
}
