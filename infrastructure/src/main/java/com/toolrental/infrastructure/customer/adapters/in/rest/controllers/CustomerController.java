package com.toolrental.infrastructure.customer.adapters.in.rest.controllers;

import com.toolrental.application.customer.command.RegisterCustomerCommand;
import com.toolrental.application.customer.dto.CustomerResponse;
import com.toolrental.application.customer.usecase.ManageCustomerUseCase;
import com.toolrental.domain.customer.model.valueobject.CustomerId;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.customer.adapters.in.rest.dtos.CustomerRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
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
@RequestMapping({"/api/v1/customers", "/api/customers"})
public class CustomerController {
  private final ManageCustomerUseCase useCase;

  public CustomerController(ManageCustomerUseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping
  public List<CustomerResponse> list(Principal principal) {
    return useCase.list(Actor.id(principal));
  }

  @GetMapping("/{id}")
  public CustomerResponse find(Principal principal, @PathVariable UUID id) {
    return useCase.find(Actor.id(principal), new CustomerId(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CustomerResponse register(
      Principal principal, @Valid @RequestBody CustomerRequest request) {
    return useCase.register(
        Actor.id(principal),
        new RegisterCustomerCommand(
            new UserId(request.userId()),
            request.personType(),
            request.documentType(),
            request.documentNumber(),
            request.billingName()));
  }

  @PutMapping("/{id}")
  public CustomerResponse update(
      Principal principal, @PathVariable UUID id, @Valid @RequestBody CustomerRequest request) {
    return useCase.update(
        Actor.id(principal),
        new CustomerId(id),
        new RegisterCustomerCommand(
            new UserId(request.userId()),
            request.personType(),
            request.documentType(),
            request.documentNumber(),
            request.billingName()));
  }
}
