package com.toolrental.infrastructure.category.adapters.in.rest.controllers;

import com.toolrental.application.category.command.RegisterCategoryCommand;
import com.toolrental.application.category.dto.CategoryResponse;
import com.toolrental.application.category.usecase.ManageCategoryUseCase;
import com.toolrental.domain.category.model.valueobject.CategoryId;
import com.toolrental.infrastructure.category.adapters.in.rest.dtos.CategoryRequest;
import com.toolrental.infrastructure.common.web.Actor;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
  private final ManageCategoryUseCase management;

  public CategoryController(ManageCategoryUseCase management) {
    this.management = management;
  }

  @GetMapping
  public List<CategoryResponse> list(Principal principal) {
    return management.list(Actor.id(principal));
  }

  @GetMapping("/{id}")
  public CategoryResponse find(Principal principal, @PathVariable UUID id) {
    return management.find(Actor.id(principal), new CategoryId(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CategoryResponse register(
      Principal principal, @Valid @RequestBody CategoryRequest request) {
    return management.register(
        Actor.id(principal), new RegisterCategoryCommand(request.name(), request.description()));
  }

  @PutMapping("/{id}")
  public CategoryResponse update(
      Principal principal, @PathVariable UUID id, @Valid @RequestBody CategoryRequest request) {
    return management.update(
        Actor.id(principal), new CategoryId(id), request.name(), request.description());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void remove(Principal principal, @PathVariable UUID id) {
    management.remove(Actor.id(principal), new CategoryId(id));
  }
}
