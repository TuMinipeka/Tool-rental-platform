package com.toolrental.infrastructure.userrole.adapters.in.rest.controllers;

import com.toolrental.application.userrole.command.RegisterUserRoleCommand;
import com.toolrental.application.userrole.dto.UserRoleResponse;
import com.toolrental.application.userrole.usecase.ManageUserRoleUseCase;
import com.toolrental.domain.role.model.valueobject.RoleId;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.domain.userrole.model.valueobject.UserRoleId;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.userrole.adapters.in.rest.dtos.UserRoleRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user-roles")
public class UserRoleController {
  private final ManageUserRoleUseCase useCase;

  public UserRoleController(ManageUserRoleUseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping
  public List<UserRoleResponse> list(Principal principal) {
    return useCase.list(Actor.id(principal));
  }

  @GetMapping("/entry")
  public UserRoleResponse find(
      Principal principal, @RequestParam UUID userId, @RequestParam Short roleId) {
    return useCase.find(
        Actor.id(principal), new UserRoleId(new UserId(userId), new RoleId(roleId)));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserRoleResponse register(
      Principal principal, @Valid @RequestBody UserRoleRequest request) {
    return useCase.register(
        Actor.id(principal),
        new RegisterUserRoleCommand(
            new UserRoleId(new UserId(request.userId()), new RoleId(request.roleId()))));
  }

  @DeleteMapping("/entry")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void remove(Principal principal, @RequestParam UUID userId, @RequestParam Short roleId) {
    useCase.remove(Actor.id(principal), new UserRoleId(new UserId(userId), new RoleId(roleId)));
  }
}
