package com.toolrental.application.catalog.usecase;

import com.toolrental.application.catalog.command.RegisterCategoryCommand;
import com.toolrental.application.catalog.dto.CategoryResponse;
import com.toolrental.domain.catalog.model.aggregate.Category;
import com.toolrental.domain.catalog.port.repository.CategoryRepository;
import com.toolrental.domain.common.exception.BusinessRuleException;
import java.time.Clock;
import java.util.Objects;

public final class RegisterCategoryUseCase {
  private final Clock clock;
  private final CategoryRepository repository;

  public RegisterCategoryUseCase(CategoryRepository repository, Clock clock) {
    this.repository = Objects.requireNonNull(repository);
    this.clock = Objects.requireNonNull(clock);
  }

  public CategoryResponse execute(RegisterCategoryCommand command) {
    var category = Category.register(command.name(), clock);
    if (repository.findAll().stream().anyMatch(c -> c.name().equals(category.name())))
      throw new BusinessRuleException("Category name already exists");
    repository.save(category);
    return CategoryResponse.from(category);
  }
}
