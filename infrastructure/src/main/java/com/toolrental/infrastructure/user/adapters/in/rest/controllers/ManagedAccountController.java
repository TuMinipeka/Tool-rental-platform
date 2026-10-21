package com.toolrental.infrastructure.user.adapters.in.rest.controllers;

import com.toolrental.application.user.command.RegisterSupplierAccountCommand;
import com.toolrental.application.user.command.RegisterUserCommand;
import com.toolrental.application.user.dto.UserResponse;
import com.toolrental.application.user.usecase.CreateManagedAccountUseCase;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.user.adapters.in.rest.dtos.ManagedSupplierAccountRequest;
import com.toolrental.infrastructure.user.adapters.in.rest.dtos.UserRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class ManagedAccountController {
  private final CreateManagedAccountUseCase accounts;

  public ManagedAccountController(CreateManagedAccountUseCase accounts) {
    this.accounts = accounts;
  }

  @PostMapping({"/api/v1/users/administrators", "/api/users/administrators"})
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse administrator(Principal principal, @Valid @RequestBody UserRequest request) {
    return accounts.administrator(
        Actor.id(principal),
        new RegisterUserCommand(request.email(), request.displayName(), request.password()));
  }

  @PostMapping({"/api/v1/suppliers/accounts", "/api/suppliers/accounts"})
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse supplier(
      Principal principal, @Valid @RequestBody ManagedSupplierAccountRequest request) {
    return accounts.supplier(
        Actor.id(principal),
        new RegisterSupplierAccountCommand(
            request.email(),
            request.displayName(),
            request.password(),
            request.documentType(),
            request.documentNumber(),
            request.businessName()));
  }
}
