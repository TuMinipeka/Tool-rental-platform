package com.toolrental.application.equipmentunit.usecase;

import com.toolrental.application.common.exception.ReferenceNotFoundApplicationException;
import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.equipmentunit.dto.EquipmentUnitResponse;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.equipmentunit.model.aggregate.EquipmentUnit;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.equipmentunit.model.valueobject.OperationalStatus;
import com.toolrental.domain.equipmentunit.port.repository.EquipmentUnitRepository;
import com.toolrental.domain.tool.port.repository.ToolRepository;
import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public final class ManageEquipmentUnitUseCase {
  private final EquipmentUnitRepository repository;
  private final UnitOfWork work;
  private final AccessPolicy access;
  private final WorkflowReferences references;
  private final Clock clock;
  private final RegisterEquipmentUnitUseCase registration;
  private final ToolRepository tools;

  public ManageEquipmentUnitUseCase(
      EquipmentUnitRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      RegisterEquipmentUnitUseCase registration,
      ToolRepository tools) {
    this.repository = repository;
    this.work = work;
    this.access = access;
    this.references = references;
    this.clock = clock;
    this.registration = registration;
    this.tools = tools;
  }

  private EquipmentUnit load(EquipmentUnitId id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new ReferenceNotFoundApplicationException("EquipmentUnit"));
  }

  public EquipmentUnitResponse find(UUID actor, EquipmentUnitId id) {
    return work.execute(
        () -> {
          var value = load(id);
          references.tool(actor, value.toolId());
          return EquipmentUnitResponse.from(value);
        });
  }

  public List<EquipmentUnitResponse> list(UUID actor) {
    return work.execute(
        () -> {
          access.requireActive(actor);
          var units = repository.findAll();
          if (access.isAdministrator(actor))
            return units.stream().map(EquipmentUnitResponse::from).toList();

          access.requireRole(actor, "SUPPLIER");
          var ownedToolIds = new HashSet<com.toolrental.domain.tool.model.valueobject.ToolId>();
          tools.findAll().stream()
              .filter(tool -> tool.supplierId().value().equals(actor))
              .forEach(tool -> ownedToolIds.add(tool.id()));
          return units.stream()
              .filter(unit -> ownedToolIds.contains(unit.toolId()))
              .map(EquipmentUnitResponse::from)
              .toList();
        });
  }

  public EquipmentUnitResponse update(
      UUID actor, EquipmentUnitId id, String serialNumber, String condition, String next) {
    return work.execute(
        () -> {
          var value = load(id);
          references.tool(actor, value.toolId());
          var changed =
              value.update(serialNumber, condition, OperationalStatus.valueOf(next), clock);
          repository.save(changed);
          return EquipmentUnitResponse.from(changed);
        });
  }

  public EquipmentUnitResponse register(
      UUID actor,
      com.toolrental.application.equipmentunit.command.RegisterEquipmentUnitCommand command,
      String serialNumber,
      String condition,
      String status) {
    return work.execute(
        () -> {
          access.requireRole(actor, "SUPPLIER");
          references.tool(actor, command.toolId());
          var result = registration.execute(command);
          return update(actor, result.id(), serialNumber, condition, status);
        });
  }
}
