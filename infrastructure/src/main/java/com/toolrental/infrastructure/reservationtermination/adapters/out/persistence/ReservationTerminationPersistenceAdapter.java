package com.toolrental.infrastructure.reservationtermination.adapters.out.persistence;

import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservationtermination.model.aggregate.ReservationTermination;
import com.toolrental.domain.reservationtermination.model.valueobject.ReservationTerminationId;
import com.toolrental.domain.reservationtermination.port.repository.ReservationTerminationRepository;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.infrastructure.reservationtermination.adapters.out.persistence.entity.ReservationTerminationJpaEntity;
import com.toolrental.infrastructure.reservationtermination.adapters.out.persistence.repository.ReservationTerminationJpaRepository;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ReservationTerminationPersistenceAdapter implements ReservationTerminationRepository {
  private final ReservationTerminationJpaRepository rows;

  public ReservationTerminationPersistenceAdapter(ReservationTerminationJpaRepository rows) {
    this.rows = rows;
  }

  private ReservationTermination map(ReservationTerminationJpaEntity r) {
    var c = Currency.getInstance(r.getCurrency());
    return ReservationTermination.restore(
        new ReservationTerminationId(r.getId()),
        new ReservationId(r.getReservationId()),
        r.getType(),
        r.getStatus(),
        new UserId(r.getRequestedBy()),
        r.getRequestedAt(),
        r.getResolvedBy() == null ? null : new UserId(r.getResolvedBy()),
        r.getResolvedAt(),
        r.getReason(),
        new Money(r.getFee(), c),
        new Money(r.getRefund(), c),
        new Money(r.getOutstanding(), c),
        r.getIdempotencyKey());
  }

  private ReservationTerminationJpaEntity map(ReservationTermination r) {
    return new ReservationTerminationJpaEntity(
        r.id().value(),
        r.reservationId().value(),
        r.type(),
        r.status(),
        r.requestedBy().value(),
        r.requestedAt(),
        r.resolvedBy() == null ? null : r.resolvedBy().value(),
        r.resolvedAt(),
        r.reason(),
        r.fee().amount(),
        r.refund().amount(),
        r.outstanding().amount(),
        r.fee().currency().getCurrencyCode(),
        r.idempotencyKey());
  }

  public Optional<ReservationTermination> findById(ReservationTerminationId id) {
    return rows.findById(id.value()).map(this::map);
  }

  public Optional<ReservationTermination> findByReservation(ReservationId id) {
    return rows.findByReservationId(id.value()).map(this::map);
  }

  public Optional<ReservationTermination> findByIdempotencyKey(String key) {
    return rows.findByIdempotencyKey(key).map(this::map);
  }

  public List<ReservationTermination> findByParticipant(UUID id) {
    return rows.findByParticipant(id).stream().map(this::map).toList();
  }

  public ReservationTermination save(ReservationTermination value) {
    return map(rows.save(value == null ? null : map(value)));
  }
}
