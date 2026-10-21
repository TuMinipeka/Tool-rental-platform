package com.toolrental.domain.invoice.port.repository;

import com.toolrental.domain.invoice.model.aggregate.Invoice;
import com.toolrental.domain.invoice.model.valueobject.InvoiceId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository {
  Optional<Invoice> findById(InvoiceId id);

  List<Invoice> findAll();

  List<Invoice> findByParticipant(UUID participantId);

  Optional<Invoice> findByReservation(ReservationId reservationId);

  Invoice save(Invoice value);

  void delete(InvoiceId id);
}
