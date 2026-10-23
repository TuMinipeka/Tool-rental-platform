package com.toolrental.infrastructure.legalacceptance.adapters.in.rest.controllers;

import com.toolrental.application.legalacceptance.command.AcceptLegalDocumentCommand;
import com.toolrental.application.legalacceptance.dto.LegalAcceptanceResponse;
import com.toolrental.application.legalacceptance.usecase.ManageLegalAcceptanceUseCase;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.legalacceptance.adapters.in.rest.dtos.LegalAcceptanceRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/legal-acceptances", "/api/legal-acceptances"})
public final class LegalAcceptanceController {
  private final ManageLegalAcceptanceUseCase useCase;

  public LegalAcceptanceController(ManageLegalAcceptanceUseCase useCase) {
    this.useCase = useCase;
  }

  @PostMapping
  public LegalAcceptanceResponse accept(
      Principal principal, @Valid @RequestBody LegalAcceptanceRequest request) {
    return useCase.accept(
        Actor.id(principal),
        new AcceptLegalDocumentCommand(
            request.documentType(), request.documentVersion(), request.purpose()));
  }

  @GetMapping("/me")
  public List<LegalAcceptanceResponse> mine(Principal principal) {
    return useCase.mine(Actor.id(principal));
  }
}
