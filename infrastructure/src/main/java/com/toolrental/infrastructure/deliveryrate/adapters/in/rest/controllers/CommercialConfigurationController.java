package com.toolrental.infrastructure.deliveryrate.adapters.in.rest.controllers;

import com.toolrental.application.cancellationpolicy.dto.CancellationPolicyResponse;
import com.toolrental.application.deliveryrate.dto.DeliveryRateResponse;
import com.toolrental.application.deliveryrate.usecase.ManageCommercialConfigurationUseCase;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.infrastructure.cancellationpolicy.adapters.in.rest.dtos.CancellationPolicyRequest;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.deliveryrate.adapters.in.rest.dtos.DeliveryRateRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Currency;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/commercial-configuration", "/api/commercial-configuration"})
public class CommercialConfigurationController {
  private final ManageCommercialConfigurationUseCase useCase;

  public CommercialConfigurationController(ManageCommercialConfigurationUseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping("/delivery-rates")
  public List<DeliveryRateResponse> rates(Principal p) {
    return useCase.rates(Actor.id(p));
  }

  @PostMapping("/delivery-rates")
  public DeliveryRateResponse rate(Principal p, @Valid @RequestBody DeliveryRateRequest r) {
    return useCase.configureRate(Actor.id(p), new Money(r.price(), Currency.getInstance("COP")));
  }

  @GetMapping("/cancellation-policies")
  public List<CancellationPolicyResponse> policies(Principal p) {
    return useCase.policies(Actor.id(p));
  }

  @PostMapping("/cancellation-policies")
  public CancellationPolicyResponse policy(
      Principal p, @Valid @RequestBody CancellationPolicyRequest r) {
    return useCase.configurePolicy(
        Actor.id(p),
        r.cutoffHours(),
        r.lateCancellationRate(),
        r.noShowRate(),
        r.noShowGraceMinutes());
  }
}
