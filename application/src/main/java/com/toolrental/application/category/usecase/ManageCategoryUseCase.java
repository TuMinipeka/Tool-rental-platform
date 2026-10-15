package com.toolrental.application.category.usecase;

import com.toolrental.application.category.dto.CategoryResponse;
import com.toolrental.application.common.exception.ReferenceNotFoundApplicationException;
import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.domain.category.model.aggregate.Category;
import com.toolrental.domain.category.model.valueobject.CategoryId;
import com.toolrental.domain.category.port.repository.CategoryRepository;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

public final class ManageCategoryUseCase {
  private final CategoryRepository repository;
  private final UnitOfWork work;
  private final AccessPolicy access;
  private final WorkflowReferences references;
  private final Clock clock;
  private final RegisterCategoryUseCase registration;

  public ManageCategoryUseCase(
      CategoryRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      RegisterCategoryUseCase registration) {
    this.repository = repository;
    this.work = work;
    this.access = access;
    this.references = references;
    this.clock = clock;
    this.registration = registration;
  }

  private Category load(CategoryId id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new ReferenceNotFoundApplicationException("Category"));
  }

  public CategoryResponse find(UUID actor, CategoryId id) {
    return work.execute(
        () -> {
          var value = load(id);
          access.requireActive(actor);
          return CategoryResponse.from(value);
        });
  }

  public List<CategoryResponse> list(UUID actor) {
    return work.execute(
        () -> {
          access.requireActive(actor);
          return repository.findAll().stream().map(CategoryResponse::from).toList();
        });
  }

  public CategoryResponse update(UUID actor, CategoryId id, String name, String description) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          var current = load(id);
          var value = current.update(name, description, clock);
          repository.save(value);
          return CategoryResponse.from(value);
        });
  }

  public void remove(UUID actor, CategoryId id) {
    work.execute(
        () -> {
          access.requireAdministrator(actor);
          load(id);
          repository.delete(id);
          return null;
        });
  }

  public CategoryResponse register(
      UUID actor, com.toolrental.application.category.command.RegisterCategoryCommand command) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          return registration.execute(command);
        });
  }
}
