package com.toolrental.infrastructure.payment.adapters.in.rest.controllers;

import com.toolrental.application.payment.command.RegisterPaymentCommand;
import com.toolrental.application.payment.dto.PaymentResponse;
import com.toolrental.application.payment.usecase.ManagePaymentUseCase;
import com.toolrental.domain.payment.model.valueobject.PaymentId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.payment.adapters.in.rest.dtos.PaymentRequest;
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
@RequestMapping("/api/payments")
public class PaymentController {
  private final ManagePaymentUseCase useCase;

  public PaymentController(ManagePaymentUseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping
  public List<PaymentResponse> list(Principal principal) {
    return useCase.list(Actor.id(principal));
  }

  @GetMapping("/{id}")
  public PaymentResponse find(Principal principal, @PathVariable UUID id) {
    return useCase.find(Actor.id(principal), new PaymentId(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PaymentResponse register(Principal principal, @Valid @RequestBody PaymentRequest request) {
    return useCase.register(
        Actor.id(principal),
        new RegisterPaymentCommand(
            new ReservationId(request.reservationId()),
            request.processor(),
            request.externalReference(),
            request.amount()));
  }

  @PatchMapping("/{id}/status")
  public PaymentResponse transition(
      Principal principal,
      @PathVariable UUID id,
      @Valid @RequestBody com.toolrental.infrastructure.common.web.StatusRequest request) {
    return useCase.transition(Actor.id(principal), new PaymentId(id), request.status());
  }
}
