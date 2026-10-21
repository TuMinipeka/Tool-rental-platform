package com.toolrental.application.supplier.usecase;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.supplier.command.RegisterSupplierCommand;
import com.toolrental.application.supplier.dto.SupplierResponse;
import com.toolrental.application.supplier.exception.SupplierNotFoundApplicationException;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.supplier.model.aggregate.Supplier;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import com.toolrental.domain.supplier.port.repository.SupplierRepository;
import com.toolrental.domain.user.port.repository.UserRepository;
import com.toolrental.domain.usersession.port.repository.UserSessionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ManageSupplierUseCase {
  private final SupplierRepository repository;
  private final UnitOfWork work;
  private final AccessPolicy access;
  private final WorkflowReferences references;
  private final Clock clock;
  private final UserRepository users;
  private final UserSessionRepository sessions;

  public ManageSupplierUseCase(
      SupplierRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      UserRepository users,
      UserSessionRepository sessions) {
    this.repository = repository;
    this.work = work;
    this.access = access;
    this.references = references;
    this.clock = clock;
    this.users = users;
    this.sessions = sessions;
  }

  private Supplier load(SupplierId id) {
    return repository.findById(id).orElseThrow(SupplierNotFoundApplicationException::new);
  }

  public SupplierResponse find(UUID actor, SupplierId id) {
    return work.execute(
        () -> {
          var value = load(id);
          access.requireOwnerOrAdministrator(actor, value.id().value());
          return SupplierResponse.from(value);
        });
  }

  public List<SupplierResponse> list(UUID actor) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          return repository.findAll().stream().map(SupplierResponse::from).toList();
        });
  }

  public SupplierResponse register(UUID actor, RegisterSupplierCommand command) {
    return work.execute(
        () -> {
          access.requireOwnerOrAdministrator(actor, command.userId().value());
          WorkflowReferences.reference(users.findById(command.userId()).isPresent(), "User");
          access.requireRole(command.userId().value(), "SUPPLIER");
          var id = new SupplierId(command.userId().value());
          if (repository.findById(id).isPresent())
            throw new BusinessRuleException("Supplier already exists");
          var value =
              Supplier.register(
                  id,
                  command.documentType(),
                  command.documentNumber(),
                  command.businessName(),
                  clock);
          var saved = repository.save(value);
          return SupplierResponse.from(saved);
        });
  }

  public SupplierResponse update(UUID actor, SupplierId id, RegisterSupplierCommand command) {
    return work.execute(
        () -> {
          var value = load(id);
          access.requireOwnerOrAdministrator(actor, value.id().value());
          WorkflowReferences.require(
              id.value().equals(command.userId().value()), "Profile identity cannot change");
          return SupplierResponse.from(
              repository.save(
                  value.update(
                      command.documentType(),
                      command.documentNumber(),
                      command.businessName(),
                      clock)));
        });
  }

  public void deactivate(UUID actor, SupplierId id) {
    work.execute(
        () -> {
          load(id);
          access.requireOwnerOrAdministrator(actor, id.value());
          var user =
              users
                  .findById(new com.toolrental.domain.user.model.valueobject.UserId(id.value()))
                  .orElseThrow(
                      () ->
                          new com.toolrental.application.common.exception
                              .ReferenceNotFoundApplicationException("User"));
          if (!"DISABLED".equals(user.accountStatus())) {
            users.save(user.transition("DISABLED", clock));
          }
          sessions.revokeAllForUser(user.id(), Instant.now(clock));
          return null;
        });
  }
}
