package com.toolrental.application.toolimage.usecase;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.toolimage.command.RegisterToolImageCommand;
import com.toolrental.application.toolimage.dto.ToolImageResponse;
import com.toolrental.application.toolimage.exception.ToolImageNotFoundApplicationException;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.toolimage.model.aggregate.ToolImage;
import com.toolrental.domain.toolimage.model.valueobject.ToolImageId;
import com.toolrental.domain.toolimage.port.repository.ToolImageRepository;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

public final class ManageToolImageUseCase {
  private final ToolImageRepository repository;
  private final UnitOfWork work;
  private final AccessPolicy access;
  private final WorkflowReferences references;
  private final Clock clock;

  public ManageToolImageUseCase(
      ToolImageRepository repository,
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

  private ToolImage load(ToolImageId id) {
    return repository.findById(id).orElseThrow(ToolImageNotFoundApplicationException::new);
  }

  public ToolImageResponse find(UUID actor, ToolImageId id) {
    return work.execute(
        () -> {
          var value = load(id);
          references.tool(actor, value.id().toolId());
          return ToolImageResponse.from(value);
        });
  }

  public List<ToolImageResponse> list(UUID actor) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          return repository.findAll().stream().map(ToolImageResponse::from).toList();
        });
  }

  public ToolImageResponse register(UUID actor, RegisterToolImageCommand command) {
    return work.execute(
        () -> {
          references.tool(actor, command.id().toolId());
          var id = command.id();
          if (repository.findById(id).isPresent())
            throw new BusinessRuleException("ToolImage already exists");
          var value = ToolImage.register(id, clock);
          var saved = repository.save(value);
          return ToolImageResponse.from(saved);
        });
  }

  public void remove(UUID actor, ToolImageId id) {
    work.execute(
        () -> {
          var value = load(id);
          references.tool(actor, value.id().toolId());
          repository.delete(id);
          return null;
        });
  }
}
