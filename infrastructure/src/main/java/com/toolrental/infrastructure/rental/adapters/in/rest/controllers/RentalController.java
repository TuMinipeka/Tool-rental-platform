package com.toolrental.infrastructure.rental.adapters.in.rest.controllers;

import com.toolrental.application.rental.command.RegisterRentalCommand;
import com.toolrental.application.rental.dto.RentalResponse;
import com.toolrental.application.rental.usecase.ManageRentalUseCase;
import com.toolrental.domain.rental.model.valueobject.RentalId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.rental.adapters.in.rest.dtos.RentalRequest;
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
@RequestMapping({"/api/v1/rentals", "/api/rentals"})
public class RentalController {
  private final ManageRentalUseCase useCase;

  public RentalController(ManageRentalUseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping
  public List<RentalResponse> list(Principal principal) {
    return useCase.list(Actor.id(principal));
  }

  @GetMapping("/{id}")
  public RentalResponse find(Principal principal, @PathVariable UUID id) {
    return useCase.find(Actor.id(principal), new RentalId(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public RentalResponse register(Principal principal, @Valid @RequestBody RentalRequest request) {
    return useCase.register(
        Actor.id(principal), new RegisterRentalCommand(new ReservationId(request.reservationId())));
  }

  @PatchMapping("/{id}/status")
  public RentalResponse transition(
      Principal principal,
      @PathVariable UUID id,
      @Valid @RequestBody com.toolrental.infrastructure.common.web.StatusRequest request) {
    return useCase.transition(Actor.id(principal), new RentalId(id), request.status());
  }
}
