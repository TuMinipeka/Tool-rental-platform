package com.toolrental.application.invoice.command;

import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import java.util.List;

public record RegisterInvoiceCommand(
    ReservationId reservationId, String number, List<InvoiceLineCommand> lines) {
  public RegisterInvoiceCommand {
    lines = List.copyOf(lines);
  }
}
