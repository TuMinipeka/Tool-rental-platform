package com.toolrental.infrastructure.payment.adapters.out.persistence;

import com.toolrental.domain.payment.model.aggregate.Payment;
import com.toolrental.domain.payment.model.valueobject.PaymentId;
import com.toolrental.domain.payment.port.repository.PaymentRepository;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.infrastructure.payment.adapters.out.persistence.mapper.PaymentPersistenceMapper;
import com.toolrental.infrastructure.payment.adapters.out.persistence.repository.PaymentJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class PaymentPersistenceAdapter implements PaymentRepository {
  private final PaymentJpaRepository rows;
  private final PaymentPersistenceMapper mapper;

  public PaymentPersistenceAdapter(PaymentJpaRepository rows, PaymentPersistenceMapper mapper) {
    this.rows = rows;
    this.mapper = mapper;
  }

  public Optional<Payment> findById(PaymentId id) {
    return rows.findLocked(mapper.key(id)).map(mapper::toDomain);
  }

  public List<Payment> findAll() {
    return rows.findAll().stream().map(mapper::toDomain).toList();
  }

  public List<Payment> findByParticipant(UUID participantId) {
    return rows.findByParticipantId(participantId).stream().map(mapper::toDomain).toList();
  }

  public List<Payment> findByReservation(ReservationId reservationId) {
    return rows.findByReservationIdOrderByRecordedAt(reservationId.value()).stream()
        .map(mapper::toDomain)
        .toList();
  }

  public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
    return rows.findByIdempotencyKey(idempotencyKey).map(mapper::toDomain);
  }

  public Optional<Payment> findByProcessorAndExternalReference(
      String processor, String externalReference) {
    return rows.findByProcessorAndExternalReference(processor, externalReference)
        .map(mapper::toDomain);
  }

  public boolean hasConfirmedStage(ReservationId reservationId, String paymentStage) {
    return rows.existsByReservationIdAndPaymentStageAndPaymentStatus(
        reservationId.value(), paymentStage, "CONFIRMED");
  }

  public Payment save(Payment value) {
    return mapper.toDomain(rows.saveAndFlush(mapper.toEntity(value)));
  }

  public void delete(PaymentId id) {
    rows.deleteById(mapper.key(id));
    rows.flush();
  }
}
