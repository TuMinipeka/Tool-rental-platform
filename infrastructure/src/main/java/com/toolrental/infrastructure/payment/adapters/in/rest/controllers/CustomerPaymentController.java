package com.toolrental.infrastructure.payment.adapters.in.rest.controllers;

import com.toolrental.application.payment.dto.CustomerPaymentResponse;
import com.toolrental.application.payment.usecase.ManagePaymentUseCase;
import com.toolrental.infrastructure.common.web.Actor;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerPaymentController {
  private final ManagePaymentUseCase useCase;

  public CustomerPaymentController(ManagePaymentUseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping({"/pagos/cliente/{id}", "/api/v1/pagos/cliente/{id}", "/api/pagos/cliente/{id}"})
  public List<CustomerPaymentResponse> history(Principal principal, @PathVariable UUID id) {
    return useCase.history(Actor.id(principal), id);
  }
}
