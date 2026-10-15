package com.toolrental.application.userrole.usecase;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.userrole.command.RegisterUserRoleCommand;
import com.toolrental.application.userrole.dto.UserRoleResponse;
import com.toolrental.application.userrole.exception.UserRoleNotFoundApplicationException;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.role.port.repository.RoleRepository;
import com.toolrental.domain.user.port.repository.UserRepository;
import com.toolrental.domain.userrole.model.aggregate.UserRole;
import com.toolrental.domain.userrole.model.valueobject.UserRoleId;
import com.toolrental.domain.userrole.port.repository.UserRoleRepository;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

public final class ManageUserRoleUseCase {
  private final UserRoleRepository repository;
  private final UnitOfWork work;
  private final AccessPolicy access;
  private final WorkflowReferences references;
  private final Clock clock;
  private final UserRepository users;
  private final RoleRepository roles;

  public ManageUserRoleUseCase(
      UserRoleRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      UserRepository users,
      RoleRepository roles) {
    this.repository = repository;
    this.work = work;
    this.access = access;
    this.references = references;
    this.clock = clock;
    this.users = users;
    this.roles = roles;
  }

  private UserRole load(UserRoleId id) {
    return repository.findById(id).orElseThrow(UserRoleNotFoundApplicationException::new);
  }

  public UserRoleResponse find(UUID actor, UserRoleId id) {
    return work.execute(
        () -> {
          var value = load(id);
          access.requireOwnerOrAdministrator(actor, value.id().userId().value());
          return UserRoleResponse.from(value);
        });
  }

  public List<UserRoleResponse> list(UUID actor) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          return repository.findAll().stream().map(UserRoleResponse::from).toList();
        });
  }

  public UserRoleResponse register(UUID actor, RegisterUserRoleCommand command) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          WorkflowReferences.reference(users.findById(command.id().userId()).isPresent(), "User");
          WorkflowReferences.reference(roles.findById(command.id().roleId()).isPresent(), "Role");
          var id = command.id();
          if (repository.findById(id).isPresent())
            throw new BusinessRuleException("UserRole already exists");
          var value = UserRole.register(id, clock);
          var saved = repository.save(value);
          return UserRoleResponse.from(saved);
        });
  }

  public void remove(UUID actor, UserRoleId id) {
    work.execute(
        () -> {
          var value = load(id);
          access.requireAdministrator(actor);
          repository.delete(id);
          return null;
        });
  }
}
