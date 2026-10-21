package com.toolrental.infrastructure.damagereport.adapters.in.rest.controllers;

import com.toolrental.application.damagereport.command.RegisterDamageReportCommand;
import com.toolrental.application.damagereport.dto.DamageReportResponse;
import com.toolrental.application.damagereport.usecase.ManageDamageReportUseCase;
import com.toolrental.domain.damagereport.model.valueobject.DamageReportId;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.damagereport.adapters.in.rest.dtos.DamageReportRequest;
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
@RequestMapping({"/api/v1/damage-reports", "/api/damage-reports"})
public class DamageReportController {
  private final ManageDamageReportUseCase useCase;

  public DamageReportController(ManageDamageReportUseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping
  public List<DamageReportResponse> list(Principal principal) {
    return useCase.list(Actor.id(principal));
  }

  @GetMapping("/{id}")
  public DamageReportResponse find(Principal principal, @PathVariable UUID id) {
    return useCase.find(Actor.id(principal), new DamageReportId(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public DamageReportResponse register(
      Principal principal, @Valid @RequestBody DamageReportRequest request) {
    return useCase.register(
        Actor.id(principal),
        new RegisterDamageReportCommand(
            new ReservationId(request.reservationId()),
            new EquipmentUnitId(request.equipmentUnitId()),
            request.description(),
            request.assessedCost()));
  }

  @PatchMapping("/{id}/status")
  public DamageReportResponse transition(
      Principal principal,
      @PathVariable UUID id,
      @Valid @RequestBody com.toolrental.infrastructure.common.web.StatusRequest request) {
    return useCase.transition(Actor.id(principal), new DamageReportId(id), request.status());
  }

  @PatchMapping("/{id}/assessment")
  public DamageReportResponse assess(
      Principal principal,
      @PathVariable UUID id,
      @Valid @RequestBody
          com.toolrental.infrastructure.damagereport.adapters.in.rest.dtos.AssessmentRequest
              request) {
    return useCase.assess(Actor.id(principal), new DamageReportId(id), request.cost());
  }
}
