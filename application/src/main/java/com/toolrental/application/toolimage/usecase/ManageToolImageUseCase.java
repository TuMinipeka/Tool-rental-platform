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
  private final com.toolrental.domain.toolimage.port.ToolImageStorage storage;

  public ManageToolImageUseCase(
      ToolImageRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      com.toolrental.domain.toolimage.port.ToolImageStorage storage) {
    this.repository = repository;
    this.work = work;
    this.access = access;
    this.references = references;
    this.clock = clock;
    this.storage = java.util.Objects.requireNonNull(storage);
  }

  public List<ToolImageResponse> listForTool(
      UUID actor, com.toolrental.domain.tool.model.valueobject.ToolId id) {
    return work.execute(
        () -> {
          references.tool(actor, id);
          return repository.findByToolId(id).stream().map(ToolImageResponse::from).toList();
        });
  }

  private void requireCapacity(com.toolrental.domain.tool.model.valueobject.ToolId id) {
    if (repository.findByToolId(id).size() >= 8)
      throw new BusinessRuleException("A tool supports at most eight images");
  }

  public ToolImageResponse upload(
      UUID actor, com.toolrental.application.toolimage.command.UploadToolImageCommand command) {
    final String[] stored = {null};
    try {
      return work.execute(
          () -> {
            references.tool(actor, command.toolId());
            requireCapacity(command.toolId());
            stored[0] = storage.store(command.content());
            return ToolImageResponse.from(
                repository.save(
                    ToolImage.register(new ToolImageId(command.toolId(), stored[0]), clock)));
          });
    } catch (RuntimeException error) {
      if (stored[0] != null) {
        try {
          storage.delete(stored[0]);
        } catch (RuntimeException cleanup) {
          error.addSuppressed(cleanup);
        }
      }
      throw error;
    }
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
          if (new com.toolrental.domain.toolimage.model.valueobject.ToolImageReference(
                  id.imageUrl())
              .managed())
            throw new IllegalArgumentException(
                "Managed images must be uploaded for their owning tool");
          requireCapacity(id.toolId());
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
    storage.delete(id.imageUrl());
  }
}
