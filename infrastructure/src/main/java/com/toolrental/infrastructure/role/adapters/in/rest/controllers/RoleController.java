package com.toolrental.infrastructure.role.adapters.in.rest.controllers;

import com.toolrental.application.role.command.RegisterRoleCommand;
import com.toolrental.application.role.dto.RoleResponse;
import com.toolrental.application.role.usecase.ManageRoleUseCase;
import com.toolrental.domain.role.model.valueobject.RoleId;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.role.adapters.in.rest.dtos.RoleRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/roles", "/api/roles"})
public class RoleController {
  private final ManageRoleUseCase useCase;

  public RoleController(ManageRoleUseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping
  public List<RoleResponse> list(Principal principal) {
    return useCase.list(Actor.id(principal));
  }

  @GetMapping("/{id}")
  public RoleResponse find(Principal principal, @PathVariable Short id) {
    return useCase.find(Actor.id(principal), new RoleId(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public RoleResponse register(Principal principal, @Valid @RequestBody RoleRequest request) {
    return useCase.register(
        Actor.id(principal), new RegisterRoleCommand(request.code(), request.description()));
  }

  @PutMapping("/{id}")
  public RoleResponse update(
      Principal principal, @PathVariable Short id, @Valid @RequestBody RoleRequest request) {
    return useCase.update(
        Actor.id(principal),
        new RoleId(id),
        new RegisterRoleCommand(request.code(), request.description()));
  }
}
