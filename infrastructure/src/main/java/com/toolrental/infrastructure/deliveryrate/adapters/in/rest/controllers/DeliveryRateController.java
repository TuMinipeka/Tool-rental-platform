package com.toolrental.infrastructure.deliveryrate.adapters.in.rest.controllers;

import com.toolrental.application.deliveryrate.dto.DeliveryRateResponse;
import com.toolrental.application.deliveryrate.usecase.ManageDeliveryRateUseCase;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.deliveryrate.adapters.in.rest.dtos.DeliveryRateRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Currency;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/commercial-configuration", "/api/commercial-configuration"})
public final class DeliveryRateController {
  private final ManageDeliveryRateUseCase useCase;

  public DeliveryRateController(ManageDeliveryRateUseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping("/delivery-rates")
  public List<DeliveryRateResponse> rates(Principal principal) {
    return useCase.list(Actor.id(principal));
  }

  @PostMapping("/delivery-rates")
  public DeliveryRateResponse configure(
      Principal principal, @Valid @RequestBody DeliveryRateRequest request) {
    return useCase.configure(
        Actor.id(principal), new Money(request.price(), Currency.getInstance("COP")));
  }
}
