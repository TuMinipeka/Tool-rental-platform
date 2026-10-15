package com.toolrental.application.rental.command;

import com.toolrental.domain.reservation.model.valueobject.ReservationId;

public record RegisterRentalCommand(ReservationId reservationId) {}
