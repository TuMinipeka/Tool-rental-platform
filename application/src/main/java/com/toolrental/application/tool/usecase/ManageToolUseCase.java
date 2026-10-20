package com.toolrental.application.tool.usecase;

import com.toolrental.application.common.exception.ReferenceNotFoundApplicationException;
import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.tool.dto.ToolResponse;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.tool.model.aggregate.Tool;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import com.toolrental.domain.tool.port.repository.ToolRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

public final class ManageToolUseCase {
  private final ToolRepository repository;
  private final UnitOfWork work;
  private final AccessPolicy access;
  private final WorkflowReferences references;
  private final Clock clock;
  private final RegisterToolUseCase registration;

  public ManageToolUseCase(
      ToolRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      RegisterToolUseCase registration) {
    this.repository = repository;
    this.work = work;
    this.access = access;
    this.references = references;
    this.clock = clock;
    this.registration = registration;
  }

  private Tool load(ToolId id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new ReferenceNotFoundApplicationException("Tool"));
  }

  public ToolResponse find(UUID actor, ToolId id) {
    return work.execute(
        () -> {
          var value = load(id);
          access.requireActive(actor);
          if (!value.published()) references.tool(actor, id);
          return ToolResponse.from(value);
        });
  }

  public List<ToolResponse> list(UUID actor) {
    return work.execute(
        () -> {
          access.requireActive(actor);
          if (access.isAdministrator(actor))
            return repository.findAll().stream().map(ToolResponse::from).toList();
          return repository.findAll().stream()
              .filter(value -> value.published() || value.supplierId().value().equals(actor))
              .map(ToolResponse::from)
              .toList();
        });
  }

  public ToolResponse update(
      UUID actor,
      ToolId id,
      String name,
      String description,
      BigDecimal amount,
      String currency,
      boolean published) {
    return work.execute(
        () -> {
          var value = load(id);
          references.tool(actor, id);
          var changed =
              value.update(
                  name,
                  description,
                  new com.toolrental.domain.common.model.valueobject.Money(
                      amount, Currency.getInstance(currency)),
                  published,
                  clock);
          repository.save(changed);
          return ToolResponse.from(changed);
        });
  }

  public void unpublish(UUID actor, ToolId id) {
    work.execute(
        () -> {
          var value = references.tool(actor, id);
          value.unpublish();
          repository.save(value);
          return null;
        });
  }

  public ToolResponse register(
      UUID actor,
      com.toolrental.application.tool.command.RegisterToolCommand command,
      boolean published) {
    return work.execute(
        () -> {
          access.requireRole(actor, "SUPPLIER");
          access.requireOwnerOrAdministrator(actor, command.supplierId().value());
          var result = registration.execute(command);
          if (!published) {
            var value = load(result.id());
            value.unpublish();
            repository.save(value);
            return ToolResponse.from(value);
          }
          return result;
        });
  }
}
