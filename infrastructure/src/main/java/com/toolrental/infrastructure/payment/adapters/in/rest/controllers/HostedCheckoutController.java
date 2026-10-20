package com.toolrental.infrastructure.payment.adapters.in.rest.controllers;

import com.toolrental.application.payment.dto.HostedCheckoutResponse;
import com.toolrental.application.payment.usecase.CreateHostedCheckoutUseCase;
import com.toolrental.application.payment.usecase.GetHostedCheckoutUseCase;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.payment.adapters.in.rest.dtos.HostedCheckoutRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/payments", "/api/payments"})
public final class HostedCheckoutController {
  private final CreateHostedCheckoutUseCase checkout;
  private final GetHostedCheckoutUseCase query;

  public HostedCheckoutController(
      CreateHostedCheckoutUseCase checkout, GetHostedCheckoutUseCase query) {
    this.checkout = checkout;
    this.query = query;
  }

  @GetMapping("/checkouts/{id}")
  public HostedCheckoutResponse state(Principal principal, @PathVariable java.util.UUID id) {
    return query.execute(Actor.id(principal), id);
  }

  @PostMapping("/checkouts")
  public HostedCheckoutResponse create(
      Principal principal,
      @RequestHeader("Idempotency-Key") String key,
      @Valid @RequestBody HostedCheckoutRequest request) {
    return checkout.execute(Actor.id(principal), new ReservationId(request.reservationId()), key);
  }
}
