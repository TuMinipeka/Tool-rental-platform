package com.toolrental.infrastructure.reservationextension.adapters.in.rest.controllers;

import com.toolrental.application.reservationextension.dto.ReservationExtensionResponse;
import com.toolrental.application.reservationextension.usecase.ManageReservationExtensionUseCase;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservationextension.model.valueobject.ReservationExtensionId;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.reservationextension.adapters.in.rest.dtos.ReservationExtensionRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/reservation-extensions", "/api/reservation-extensions"})
public class ReservationExtensionController {
  private final ManageReservationExtensionUseCase useCase;

  public ReservationExtensionController(ManageReservationExtensionUseCase useCase) {
    this.useCase = useCase;
  }

  @PostMapping("/reservations/{id}")
  public ReservationExtensionResponse request(
      Principal p,
      @PathVariable UUID id,
      @RequestHeader("Idempotency-Key") String key,
      @Valid @RequestBody ReservationExtensionRequest body) {
    return useCase.request(Actor.id(p), new ReservationId(id), body.requestedReturnDate(), key);
  }

  @PostMapping("/{id}/approval")
  public ReservationExtensionResponse approve(Principal p, @PathVariable UUID id) {
    return useCase.resolve(Actor.id(p), new ReservationExtensionId(id), true);
  }

  @PostMapping("/{id}/rejection")
  public ReservationExtensionResponse reject(Principal p, @PathVariable UUID id) {
    return useCase.resolve(Actor.id(p), new ReservationExtensionId(id), false);
  }

  @GetMapping("/reservations/{id}")
  public List<ReservationExtensionResponse> list(Principal p, @PathVariable UUID id) {
    return useCase.list(Actor.id(p), new ReservationId(id));
  }
}
