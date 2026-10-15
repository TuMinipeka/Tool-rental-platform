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
import java.time.Clock;
import java.util.List;
import java.util.UUID;

public final class ManageEquipmentUnitUseCase {
  private final EquipmentUnitRepository repository;
  private final UnitOfWork work;
  private final AccessPolicy access;
  private final WorkflowReferences references;
  private final Clock clock;
  private final RegisterEquipmentUnitUseCase registration;

  public ManageEquipmentUnitUseCase(
      EquipmentUnitRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      RegisterEquipmentUnitUseCase registration) {
    this.repository = repository;
    this.work = work;
    this.access = access;
    this.references = references;
    this.clock = clock;
    this.registration = registration;
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
          access.requireAdministrator(actor);
          return repository.findAll().stream().map(EquipmentUnitResponse::from).toList();
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
