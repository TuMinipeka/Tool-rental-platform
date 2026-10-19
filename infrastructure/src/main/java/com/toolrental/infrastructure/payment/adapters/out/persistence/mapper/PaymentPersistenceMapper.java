package com.toolrental.infrastructure.payment.adapters.out.persistence.mapper;

import com.toolrental.domain.payment.model.aggregate.Payment;
import com.toolrental.domain.payment.model.valueobject.PaymentId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.infrastructure.payment.adapters.out.persistence.entity.PaymentJpaEntity;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PaymentPersistenceMapper {
  public UUID key(PaymentId id) {
    return id.value();
  }

  public Payment toDomain(PaymentJpaEntity row) {
    return Payment.restore(
        new PaymentId(row.getId()),
        new ReservationId(row.getReservationId()),
        row.getProcessor(),
        row.getExternalReference(),
        row.getAmount(),
        row.getPaymentStatus(),
        row.getRecordedAt(),
        row.getConfirmedAt(),
        row.getPaymentMethod(),
        row.getPaymentStage(),
        row.getIdempotencyKey(),
        row.getCardNetwork(),
        row.getCardLastFour(),
        row.getCardExpiryMonth(),
        row.getCardExpiryYear(),
        row.getAdjustmentType(),
        row.getAdjustmentReference());
  }

  public PaymentJpaEntity toEntity(Payment value) {
    return new PaymentJpaEntity(
        key(value.id()),
        value.reservationId().value(),
        value.processor(),
        value.externalReference(),
        value.amount(),
        value.paymentStatus(),
        value.recordedAt(),
        value.confirmedAt(),
        value.paymentMethod(),
        value.paymentStage(),
        value.idempotencyKey(),
        value.cardNetwork(),
        value.cardLastFour(),
        value.cardExpiryMonth(),
        value.cardExpiryYear(),
        value.adjustmentType(),
        value.adjustmentReference());
  }
}
