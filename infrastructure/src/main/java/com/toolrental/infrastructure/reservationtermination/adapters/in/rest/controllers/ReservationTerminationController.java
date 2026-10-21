package com.toolrental.infrastructure.reservationtermination.adapters.in.rest.controllers;

import com.toolrental.application.reservationtermination.dto.ReservationTerminationResponse;
import com.toolrental.application.reservationtermination.usecase.ManageReservationTerminationUseCase;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservationtermination.model.valueobject.ReservationTerminationId;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.reservation.adapters.in.rest.dtos.TerminationRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/reservation-terminations", "/api/reservation-terminations"})
public class ReservationTerminationController {
  private final ManageReservationTerminationUseCase useCase;

  public ReservationTerminationController(ManageReservationTerminationUseCase useCase) {
    this.useCase = useCase;
  }

  @PostMapping("/reservations/{id}/cancellation")
  public ReservationTerminationResponse cancel(
      Principal p, @PathVariable UUID id, @RequestHeader("Idempotency-Key") String key) {
    return useCase.cancel(Actor.id(p), new ReservationId(id), key);
  }

  @PostMapping("/reservations/{id}/no-show")
  public ReservationTerminationResponse noShow(
      Principal p,
      @PathVariable UUID id,
      @RequestHeader("Idempotency-Key") String key,
      @Valid @RequestBody TerminationRequest body) {
    return useCase.noShow(Actor.id(p), new ReservationId(id), body.reason(), key);
  }

  @PostMapping("/reservations/{id}/supplier-failure")
  public ReservationTerminationResponse supplierFailure(
      Principal p,
      @PathVariable UUID id,
      @RequestHeader("Idempotency-Key") String key,
      @Valid @RequestBody TerminationRequest body) {
    return useCase.supplierFailure(Actor.id(p), new ReservationId(id), body.reason(), key);
  }

  @PostMapping("/reservations/{id}/withdrawal")
  public ReservationTerminationResponse withdrawal(
      Principal p,
      @PathVariable UUID id,
      @RequestHeader("Idempotency-Key") String key,
      @Valid @RequestBody TerminationRequest body) {
    return useCase.requestWithdrawal(Actor.id(p), new ReservationId(id), body.reason(), key);
  }

  @PostMapping("/reservations/{id}/external-review")
  public ReservationTerminationResponse externalReview(
      Principal p,
      @PathVariable UUID id,
      @RequestHeader("Idempotency-Key") String key,
      @Valid @RequestBody TerminationRequest body) {
    return useCase.requestExternalReview(Actor.id(p), new ReservationId(id), body.reason(), key);
  }

  @PostMapping("/{id}/approval")
  public ReservationTerminationResponse approve(Principal p, @PathVariable UUID id) {
    return useCase.resolveWithdrawal(Actor.id(p), new ReservationTerminationId(id), true);
  }

  @PostMapping("/{id}/rejection")
  public ReservationTerminationResponse reject(Principal p, @PathVariable UUID id) {
    return useCase.resolveWithdrawal(Actor.id(p), new ReservationTerminationId(id), false);
  }

  @GetMapping
  public List<ReservationTerminationResponse> list(Principal p) {
    return useCase.list(Actor.id(p));
  }
}
