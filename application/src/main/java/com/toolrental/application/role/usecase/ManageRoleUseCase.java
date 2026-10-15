package com.toolrental.application.role.usecase;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.role.command.RegisterRoleCommand;
import com.toolrental.application.role.dto.RoleResponse;
import com.toolrental.application.role.exception.RoleNotFoundApplicationException;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.role.model.aggregate.Role;
import com.toolrental.domain.role.model.valueobject.RoleId;
import com.toolrental.domain.role.port.repository.RoleRepository;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

public final class ManageRoleUseCase {
  private final RoleRepository repository;
  private final UnitOfWork work;
  private final AccessPolicy access;
  private final WorkflowReferences references;
  private final Clock clock;

  public ManageRoleUseCase(
      RoleRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock) {
    this.repository = repository;
    this.work = work;
    this.access = access;
    this.references = references;
    this.clock = clock;
  }

  private Role load(RoleId id) {
    return repository.findById(id).orElseThrow(RoleNotFoundApplicationException::new);
  }

  public RoleResponse find(UUID actor, RoleId id) {
    return work.execute(
        () -> {
          var value = load(id);
          access.requireAdministrator(actor);
          return RoleResponse.from(value);
        });
  }

  public List<RoleResponse> list(UUID actor) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          return repository.findAll().stream().map(RoleResponse::from).toList();
        });
  }

  public RoleResponse register(UUID actor, RegisterRoleCommand command) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          var id = new RoleId(null);
          var value = Role.register(id, command.code(), command.description(), clock);
          var saved = repository.save(value);
          return RoleResponse.from(saved);
        });
  }

  public RoleResponse update(UUID actor, RoleId id, RegisterRoleCommand command) {
    return work.execute(
        () -> {
          var value = load(id);
          access.requireAdministrator(actor);
          return RoleResponse.from(
              repository.save(value.update(command.code(), command.description(), clock)));
        });
  }
}
