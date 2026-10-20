package com.toolrental.infrastructure.supplier.adapters.in.rest.controllers;

import com.toolrental.application.supplier.command.RegisterSupplierCommand;
import com.toolrental.application.supplier.dto.SupplierResponse;
import com.toolrental.application.supplier.usecase.ManageSupplierUseCase;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.supplier.adapters.in.rest.dtos.SupplierRequest;
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
@RequestMapping({"/api/v1/suppliers", "/api/suppliers"})
public class SupplierController {
  private final ManageSupplierUseCase useCase;

  public SupplierController(ManageSupplierUseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping
  public List<SupplierResponse> list(Principal principal) {
    return useCase.list(Actor.id(principal));
  }

  @GetMapping("/{id}")
  public SupplierResponse find(Principal principal, @PathVariable UUID id) {
    return useCase.find(Actor.id(principal), new SupplierId(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SupplierResponse register(
      Principal principal, @Valid @RequestBody SupplierRequest request) {
    return useCase.register(
        Actor.id(principal),
        new RegisterSupplierCommand(
            new UserId(request.userId()),
            request.documentType(),
            request.documentNumber(),
            request.businessName()));
  }

  @PutMapping("/{id}")
  public SupplierResponse update(
      Principal principal, @PathVariable UUID id, @Valid @RequestBody SupplierRequest request) {
    return useCase.update(
        Actor.id(principal),
        new SupplierId(id),
        new RegisterSupplierCommand(
            new UserId(request.userId()),
            request.documentType(),
            request.documentNumber(),
            request.businessName()));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deactivate(Principal principal, @PathVariable UUID id) {
    useCase.deactivate(Actor.id(principal), new SupplierId(id));
  }
}
