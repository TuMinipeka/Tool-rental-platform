package com.toolrental.infrastructure.tool.adapters.in.rest.controllers;

import com.toolrental.application.tool.command.RegisterToolCommand;
import com.toolrental.application.tool.dto.ToolResponse;
import com.toolrental.application.tool.usecase.ManageToolUseCase;
import com.toolrental.domain.category.model.valueobject.CategoryId;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.tool.adapters.in.rest.dtos.ToolRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Currency;
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
@RequestMapping({"/api/v1/tools", "/api/tools"})
public class ToolController {
  private final ManageToolUseCase management;

  public ToolController(ManageToolUseCase management) {
    this.management = management;
  }

  @GetMapping
  public List<ToolResponse> list(Principal principal) {
    return management.list(Actor.id(principal));
  }

  @GetMapping("/{id}")
  public ToolResponse find(Principal principal, @PathVariable UUID id) {
    return management.find(Actor.id(principal), new ToolId(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ToolResponse register(Principal principal, @Valid @RequestBody ToolRequest request) {
    var actor = Actor.id(principal);
    return management.register(
        actor,
        new RegisterToolCommand(
            new com.toolrental.domain.supplier.model.valueobject.SupplierId(actor),
            new CategoryId(request.categoryId()),
            request.name(),
            request.description(),
            request.serviceCity(),
            new com.toolrental.domain.common.model.valueobject.Money(
                request.dailyRate(), Currency.getInstance(request.currency())),
            request.imageUrls().stream()
                .map(java.net.URI::create)
                .collect(java.util.stream.Collectors.toSet())),
        request.published());
  }

  @PutMapping("/{id}")
  public ToolResponse update(
      Principal principal,
      @PathVariable UUID id,
      @Valid @RequestBody
          com.toolrental.infrastructure.tool.adapters.in.rest.dtos.ToolUpdateRequest request) {
    return management.update(
        Actor.id(principal),
        new ToolId(id),
        request.name(),
        request.description(),
        request.serviceCity(),
        request.dailyRate(),
        request.currency(),
        request.published());
  }

  @org.springframework.web.bind.annotation.PatchMapping("/{id}/publication")
  public ToolResponse publication(
      Principal principal,
      @PathVariable UUID id,
      @Valid @RequestBody
          com.toolrental.infrastructure.tool.adapters.in.rest.dtos.ToolPublicationRequest request) {
    return management.publish(Actor.id(principal), new ToolId(id), request.published());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unpublish(Principal principal, @PathVariable UUID id) {
    management.unpublish(Actor.id(principal), new ToolId(id));
  }
}
