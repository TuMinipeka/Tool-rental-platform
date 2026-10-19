package com.toolrental.infrastructure.unitdelivery.adapters.in.rest.controllers;

import com.toolrental.application.unitdelivery.command.RegisterUnitDeliveryCommand;
import com.toolrental.application.unitdelivery.dto.UnitDeliveryResponse;
import com.toolrental.application.unitdelivery.usecase.ManageUnitDeliveryUseCase;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.unitdelivery.model.valueobject.UnitDeliveryId;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.unitdelivery.adapters.in.rest.dtos.UnitDeliveryRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/unit-deliveries", "/api/unit-deliveries"})
public class UnitDeliveryController {
  private final ManageUnitDeliveryUseCase useCase;

  public UnitDeliveryController(ManageUnitDeliveryUseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping
  public List<UnitDeliveryResponse> list(Principal principal) {
    return useCase.list(Actor.id(principal));
  }

  @GetMapping("/entry")
  public UnitDeliveryResponse find(
      Principal principal, @RequestParam UUID reservationId, @RequestParam UUID equipmentUnitId) {
    return useCase.find(
        Actor.id(principal),
        new UnitDeliveryId(new ReservationId(reservationId), new EquipmentUnitId(equipmentUnitId)));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UnitDeliveryResponse register(
      Principal principal, @Valid @RequestBody UnitDeliveryRequest request) {
    return useCase.register(
        Actor.id(principal),
        new RegisterUnitDeliveryCommand(
            new UnitDeliveryId(
                new ReservationId(request.reservationId()),
                new EquipmentUnitId(request.equipmentUnitId())),
            request.scheduledAt(),
            request.outboundCondition(),
            request.addressLine(),
            request.city(),
            request.region(),
            request.country()));
  }

  @PatchMapping("/entry/completion")
  public UnitDeliveryResponse complete(
      Principal principal,
      @RequestParam UUID reservationId,
      @RequestParam UUID equipmentUnitId,
      @Valid @RequestBody
          com.toolrental.infrastructure.unitdelivery.adapters.in.rest.dtos.DeliveryCompletionRequest
              request) {
    return useCase.complete(
        Actor.id(principal),
        new UnitDeliveryId(new ReservationId(reservationId), new EquipmentUnitId(equipmentUnitId)),
        request.condition());
  }

  @PatchMapping("/entry/failure")
  public UnitDeliveryResponse fail(
      Principal principal,
      @RequestParam UUID reservationId,
      @RequestParam UUID equipmentUnitId,
      @Valid @RequestBody
          com.toolrental.infrastructure.unitdelivery.adapters.in.rest.dtos.DeliveryFailureRequest
              request) {
    return useCase.fail(
        Actor.id(principal),
        new UnitDeliveryId(new ReservationId(reservationId), new EquipmentUnitId(equipmentUnitId)),
        request.type(),
        request.reason());
  }
}
