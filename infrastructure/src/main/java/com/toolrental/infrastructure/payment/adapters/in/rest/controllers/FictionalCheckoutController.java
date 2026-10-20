package com.toolrental.infrastructure.payment.adapters.in.rest.controllers;

import com.toolrental.application.payment.dto.HostedCheckoutResponse;
import com.toolrental.application.payment.usecase.DecideFictionalCheckoutUseCase;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.payment.adapters.in.rest.dtos.FictionalPaymentDecisionRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/payments/fictional/checkouts", "/api/payments/fictional/checkouts"})
@ConditionalOnProperty(name = "payments.provider", havingValue = "fictional", matchIfMissing = true)
public final class FictionalCheckoutController {
  private final DecideFictionalCheckoutUseCase decisions;

  public FictionalCheckoutController(DecideFictionalCheckoutUseCase decisions) {
    this.decisions = decisions;
  }

  @PostMapping("/{id}/decision")
  public HostedCheckoutResponse decide(
      Principal principal,
      @PathVariable UUID id,
      @Valid @RequestBody FictionalPaymentDecisionRequest request) {
    return decisions.execute(Actor.id(principal), id, request.outcome().name());
  }
}
