package com.toolrental.application.reservation.usecase;

import com.toolrental.application.equipmentunit.exception.EquipmentUnitNotFoundApplicationException;
import com.toolrental.application.supplier.exception.SupplierNotFoundApplicationException;
import com.toolrental.application.tool.exception.ToolNotFoundApplicationException;
import com.toolrental.application.reservation.command.RequestReservationCommand;
import com.toolrental.application.reservation.dto.ReservationResponse;
import com.toolrental.application.reservation.exception.CustomerNotFoundApplicationException;
import com.toolrental.domain.equipmentunit.port.repository.EquipmentUnitRepository;
import com.toolrental.domain.tool.port.repository.ToolRepository;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.port.ParticipantsPort;
import com.toolrental.domain.reservation.model.aggregate.Reservation;
import com.toolrental.domain.reservationitem.model.entity.ReservationItem;
import com.toolrental.domain.reservation.port.repository.ReservationRepository;
import java.time.Clock;
import java.util.Objects;

public final class RequestReservationUseCase {
  private final ReservationRepository reservations;
  private final EquipmentUnitRepository equipmentUnits;
  private final ToolRepository tools;
  private final ParticipantsPort participants;
  private final Clock clock;

  public RequestReservationUseCase(
      ReservationRepository r,
      EquipmentUnitRepository u,
      ToolRepository h,
      ParticipantsPort p,
      Clock clock) {
    reservations = Objects.requireNonNull(r);
    equipmentUnits = Objects.requireNonNull(u);
    tools = Objects.requireNonNull(h);
    participants = Objects.requireNonNull(p);
    this.clock = Objects.requireNonNull(clock);
  }

  public ReservationResponse execute(RequestReservationCommand c) {
    if (!participants.isCustomerActive(c.customerId()))
      throw new CustomerNotFoundApplicationException(c.customerId());
    var items =
        c.equipmentUnits().stream()
            .map(
                id -> {
                  var u =
                      equipmentUnits
                          .findById(id)
                          .orElseThrow(() -> new EquipmentUnitNotFoundApplicationException(id));
                  var h =
                      tools
                          .findById(u.toolId())
                          .orElseThrow(() -> new ToolNotFoundApplicationException(u.toolId()));
                  if (!u.isOperational() || !h.published())
                    throw new BusinessRuleException("Equipment unit is not rentable");
                  if (!participants.isSupplierActive(h.supplierId()))
                    throw new SupplierNotFoundApplicationException(h.supplierId());
                  return new ReservationItem(id, h.supplierId(), h.dailyRate());
                })
            .toList();
    var reservation = Reservation.request(c.customerId(), c.period(), items, clock);
    reservations.addRequest(reservation);
    return ReservationResponse.from(reservation);
  }
}
