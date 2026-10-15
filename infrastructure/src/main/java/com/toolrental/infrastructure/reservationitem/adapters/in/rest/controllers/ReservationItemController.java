package com.toolrental.infrastructure.reservationitem.adapters.in.rest.controllers;

import com.toolrental.application.reservationitem.dto.ReservationItemResponse;
import com.toolrental.application.reservationitem.usecase.ManageReservationItemUseCase;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservationitem.model.valueobject.ReservationItemId;
import com.toolrental.infrastructure.common.web.Actor;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservation-items")
public class ReservationItemController {
  private final ManageReservationItemUseCase useCase;

  public ReservationItemController(ManageReservationItemUseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping
  public List<ReservationItemResponse> list(Principal principal) {
    return useCase.list(Actor.id(principal));
  }

  @GetMapping("/entry")
  public ReservationItemResponse find(
      Principal principal, @RequestParam UUID reservationId, @RequestParam UUID equipmentUnitId) {
    return useCase.find(
        Actor.id(principal),
        new ReservationItemId(
            new ReservationId(reservationId), new EquipmentUnitId(equipmentUnitId)));
  }
}
