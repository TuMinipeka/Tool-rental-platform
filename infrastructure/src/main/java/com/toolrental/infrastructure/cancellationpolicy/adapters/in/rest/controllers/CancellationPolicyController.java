package com.toolrental.infrastructure.cancellationpolicy.adapters.in.rest.controllers;

import com.toolrental.application.cancellationpolicy.dto.CancellationPolicyResponse;
import com.toolrental.application.cancellationpolicy.usecase.ManageCancellationPolicyUseCase;
import com.toolrental.infrastructure.cancellationpolicy.adapters.in.rest.dtos.CancellationPolicyRequest;
import com.toolrental.infrastructure.common.web.Actor;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/commercial-configuration", "/api/commercial-configuration"})
public final class CancellationPolicyController {
  private final ManageCancellationPolicyUseCase useCase;

  public CancellationPolicyController(ManageCancellationPolicyUseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping("/cancellation-policies")
  public List<CancellationPolicyResponse> policies(Principal principal) {
    return useCase.list(Actor.id(principal));
  }

  @PostMapping("/cancellation-policies")
  public CancellationPolicyResponse configure(
      Principal principal, @Valid @RequestBody CancellationPolicyRequest request) {
    return useCase.configure(
        Actor.id(principal),
        request.cutoffHours(),
        request.lateCancellationRate(),
        request.noShowRate(),
        request.noShowGraceMinutes());
  }
}
