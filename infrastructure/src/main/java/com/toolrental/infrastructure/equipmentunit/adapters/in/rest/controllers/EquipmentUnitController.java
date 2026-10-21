package com.toolrental.infrastructure.equipmentunit.adapters.in.rest.controllers;

import com.toolrental.application.equipmentunit.command.RegisterEquipmentUnitCommand;
import com.toolrental.application.equipmentunit.dto.EquipmentUnitResponse;
import com.toolrental.application.equipmentunit.usecase.ManageEquipmentUnitUseCase;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.equipmentunit.adapters.in.rest.dtos.EquipmentUnitRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/equipment-units", "/api/equipment-units"})
public class EquipmentUnitController {
  private final ManageEquipmentUnitUseCase management;

  public EquipmentUnitController(ManageEquipmentUnitUseCase management) {
    this.management = management;
  }

  @GetMapping
  public List<EquipmentUnitResponse> list(Principal principal) {
    return management.list(Actor.id(principal));
  }

  @GetMapping("/{id}")
  public EquipmentUnitResponse find(Principal principal, @PathVariable UUID id) {
    return management.find(Actor.id(principal), new EquipmentUnitId(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public EquipmentUnitResponse register(
      Principal principal, @Valid @RequestBody EquipmentUnitRequest request) {
    return management.register(
        Actor.id(principal),
        new RegisterEquipmentUnitCommand(new ToolId(request.toolId()), request.inventoryCode()),
        request.serialNumber(),
        request.currentCondition(),
        request.status());
  }

  @PutMapping("/{id}")
  public EquipmentUnitResponse update(
      Principal principal,
      @PathVariable UUID id,
      @Valid @RequestBody
          com.toolrental.infrastructure.equipmentunit.adapters.in.rest.dtos
                  .EquipmentUnitUpdateRequest
              request) {
    return management.update(
        Actor.id(principal),
        new EquipmentUnitId(id),
        request.serialNumber(),
        request.currentCondition(),
        request.status());
  }
}
