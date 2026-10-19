package com.toolrental.domain.payment.port.repository;

import com.toolrental.domain.payment.model.aggregate.Payment;
import com.toolrental.domain.payment.model.valueobject.PaymentId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
  Optional<Payment> findById(PaymentId id);

  List<Payment> findAll();

  List<Payment> findByParticipant(UUID participantId);

  List<Payment> findByReservation(
      com.toolrental.domain.reservation.model.valueobject.ReservationId reservationId);

  Optional<Payment> findByIdempotencyKey(String idempotencyKey);

  boolean hasConfirmedStage(
      com.toolrental.domain.reservation.model.valueobject.ReservationId reservationId,
      String paymentStage);

  Payment save(Payment value);

  void delete(PaymentId id);
}
