package com.toolrental.infrastructure.user.adapters.in.rest.controllers;

import com.toolrental.application.user.command.RegisterUserCommand;
import com.toolrental.application.user.dto.UserResponse;
import com.toolrental.application.user.usecase.ManageUserUseCase;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.user.adapters.in.rest.dtos.UserRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/users", "/api/users"})
public class UserController {
  private final ManageUserUseCase useCase;

  public UserController(ManageUserUseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping
  public List<UserResponse> list(Principal principal) {
    return useCase.list(Actor.id(principal));
  }

  @GetMapping("/{id}")
  public UserResponse find(Principal principal, @PathVariable UUID id) {
    return useCase.find(Actor.id(principal), new UserId(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse register(Principal principal, @Valid @RequestBody UserRequest request) {
    return useCase.register(
        Actor.id(principal),
        new RegisterUserCommand(request.email(), request.displayName(), request.password()));
  }

  @PatchMapping("/{id}/status")
  public UserResponse transition(
      Principal principal,
      @PathVariable UUID id,
      @Valid @RequestBody com.toolrental.infrastructure.common.web.StatusRequest request) {
    return useCase.transition(Actor.id(principal), new UserId(id), request.status());
  }
}
