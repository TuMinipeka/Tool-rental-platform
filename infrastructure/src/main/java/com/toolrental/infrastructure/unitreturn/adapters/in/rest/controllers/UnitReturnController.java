package com.toolrental.infrastructure.unitreturn.adapters.in.rest.controllers;

import com.toolrental.application.unitreturn.command.RegisterUnitReturnCommand;
import com.toolrental.application.unitreturn.dto.UnitReturnResponse;
import com.toolrental.application.unitreturn.usecase.ManageUnitReturnUseCase;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.unitreturn.model.valueobject.UnitReturnId;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.unitreturn.adapters.in.rest.dtos.UnitReturnRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/unit-returns", "/api/unit-returns"})
public class UnitReturnController {
  private final ManageUnitReturnUseCase useCase;

  public UnitReturnController(ManageUnitReturnUseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping
  public List<UnitReturnResponse> list(Principal principal) {
    return useCase.list(Actor.id(principal));
  }

  @GetMapping("/entry")
  public UnitReturnResponse find(
      Principal principal, @RequestParam UUID reservationId, @RequestParam UUID equipmentUnitId) {
    return useCase.find(
        Actor.id(principal),
        new UnitReturnId(new ReservationId(reservationId), new EquipmentUnitId(equipmentUnitId)));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UnitReturnResponse register(
      Principal principal, @Valid @RequestBody UnitReturnRequest request) {
    return useCase.register(
        Actor.id(principal),
        new RegisterUnitReturnCommand(
            new UnitReturnId(
                new ReservationId(request.reservationId()),
                new EquipmentUnitId(request.equipmentUnitId())),
            request.inboundCondition(),
            request.notes()));
  }
}
