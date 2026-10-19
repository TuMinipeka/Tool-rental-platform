package com.toolrental.infrastructure.reservation.adapters.in.rest.controllers;

import com.toolrental.application.reservation.command.QuoteReservationCommand;
import com.toolrental.application.reservation.dto.ReservationQuoteResponse;
import com.toolrental.application.reservation.usecase.QuoteReservationUseCase;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.reservation.adapters.in.rest.dtos.ReservationQuoteRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quotes/reservations")
public final class ReservationQuoteController {
  private final QuoteReservationUseCase quote;

  public ReservationQuoteController(QuoteReservationUseCase quote) {
    this.quote = quote;
  }

  @PostMapping
  public ReservationQuoteResponse quote(
      Principal principal, @Valid @RequestBody ReservationQuoteRequest request) {
    return quote.execute(
        Actor.id(principal),
        new QuoteReservationCommand(
            new ReservationPeriod(request.startDate(), request.returnDate()),
            request.equipmentUnitIds().stream().map(EquipmentUnitId::new).toList()));
  }
}
