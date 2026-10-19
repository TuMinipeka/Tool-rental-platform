package com.toolrental.infrastructure.refund.adapters.out.persistence;

import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.payment.model.valueobject.PaymentId;
import com.toolrental.domain.refund.model.aggregate.Refund;
import com.toolrental.domain.refund.model.valueobject.RefundId;
import com.toolrental.domain.refund.port.repository.RefundRepository;
import com.toolrental.domain.reservationtermination.model.valueobject.ReservationTerminationId;
import com.toolrental.infrastructure.refund.adapters.out.persistence.entity.RefundJpaEntity;
import com.toolrental.infrastructure.refund.adapters.out.persistence.repository.RefundJpaRepository;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class RefundPersistenceAdapter implements RefundRepository {
  private final RefundJpaRepository rows;

  public RefundPersistenceAdapter(RefundJpaRepository rows) {
    this.rows = rows;
  }

  private Refund map(RefundJpaEntity r) {
    return Refund.restore(
        new RefundId(r.getId()),
        new PaymentId(r.getPaymentId()),
        r.getTerminationId() == null ? null : new ReservationTerminationId(r.getTerminationId()),
        new Money(r.getAmount(), Currency.getInstance(r.getCurrency())),
        r.getStatus(),
        r.getKey(),
        r.getRecordedAt(),
        r.getConfirmedAt());
  }

  private RefundJpaEntity map(Refund r) {
    return new RefundJpaEntity(
        r.id().value(),
        r.paymentId().value(),
        r.terminationId() == null ? null : r.terminationId().value(),
        r.amount().amount(),
        r.amount().currency().getCurrencyCode(),
        r.status(),
        r.idempotencyKey(),
        r.recordedAt(),
        r.confirmedAt());
  }

  public Optional<Refund> findById(RefundId id) {
    return rows.findById(id.value()).map(this::map);
  }

  public Optional<Refund> findByIdempotencyKey(String key) {
    return rows.findByKey(key).map(this::map);
  }

  public List<Refund> findByPayment(PaymentId id) {
    return rows.findByPaymentIdOrderByRecordedAt(id.value()).stream().map(this::map).toList();
  }

  public List<Refund> findByTermination(ReservationTerminationId id) {
    return rows.findByTerminationIdOrderByRecordedAt(id.value()).stream().map(this::map).toList();
  }

  public Refund save(Refund value) {
    return map(rows.save(map(value)));
  }
}
