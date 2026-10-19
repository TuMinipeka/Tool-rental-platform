package com.toolrental.domain.reservation.port.repository;

import com.toolrental.domain.customer.model.valueobject.CustomerId;
import com.toolrental.domain.reservation.model.aggregate.Reservation;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import java.util.List;
import java.util.Optional;

/** Creates requests only. State transitions must use AtomicReservationManagement. */
public interface ReservationRepository {
  void addRequest(Reservation reservation);

  Optional<Reservation> findById(ReservationId id);

  List<Reservation> findByCustomer(CustomerId customerId);

  List<Reservation> findBySupplier(SupplierId supplierId);
}
