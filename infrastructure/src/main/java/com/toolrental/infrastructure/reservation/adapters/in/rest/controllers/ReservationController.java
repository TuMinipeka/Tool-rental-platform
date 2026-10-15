package com.toolrental.infrastructure.reservation.adapters.in.rest.controllers;

import com.toolrental.application.reservation.command.RequestReservationCommand;
import com.toolrental.application.reservation.dto.ReservationResponse;
import com.toolrental.application.reservation.usecase.CancelReservationUseCase;
import com.toolrental.application.reservation.usecase.ConfirmReservationUseCase;
import com.toolrental.application.reservation.usecase.QueryReservationUseCase;
import com.toolrental.application.reservation.usecase.RejectReservationUseCase;
import com.toolrental.application.reservation.usecase.RequestReservationUseCase;
import com.toolrental.domain.customer.model.valueobject.CustomerId;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.reservation.adapters.in.rest.dtos.ReservationRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
  private final RequestReservationUseCase request;
  private final ConfirmReservationUseCase confirm;
  private final RejectReservationUseCase reject;
  private final CancelReservationUseCase cancel;
  private final QueryReservationUseCase query;

  public ReservationController(
      RequestReservationUseCase request,
      ConfirmReservationUseCase confirm,
      RejectReservationUseCase reject,
      CancelReservationUseCase cancel,
      QueryReservationUseCase query) {
    this.request = request;
    this.confirm = confirm;
    this.reject = reject;
    this.cancel = cancel;
    this.query = query;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ReservationResponse request(
      Principal principal, @Valid @RequestBody ReservationRequest body) {
    return request.execute(
        new RequestReservationCommand(
            new CustomerId(Actor.id(principal)),
            new ReservationPeriod(body.startDate(), body.returnDate()),
            body.equipmentUnitIds().stream().map(EquipmentUnitId::new).toList()));
  }

  @GetMapping("/{id}")
  public ReservationResponse find(Principal principal, @PathVariable UUID id) {
    return query.execute(Actor.id(principal), new ReservationId(id));
  }

  @PostMapping("/{id}/confirmation")
  public ReservationResponse confirm(Principal principal, @PathVariable UUID id) {
    return confirm.execute(new ReservationId(id), new SupplierId(Actor.id(principal)));
  }

  @PostMapping("/{id}/rejection")
  public ReservationResponse reject(Principal principal, @PathVariable UUID id) {
    return reject.execute(new ReservationId(id), new SupplierId(Actor.id(principal)));
  }

  @PostMapping("/{id}/cancellation")
  public ReservationResponse cancel(Principal principal, @PathVariable UUID id) {
    return cancel.execute(new ReservationId(id), new CustomerId(Actor.id(principal)));
  }
}
