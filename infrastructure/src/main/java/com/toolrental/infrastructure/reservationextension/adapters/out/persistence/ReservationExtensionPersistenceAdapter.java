package com.toolrental.infrastructure.reservationextension.adapters.out.persistence;

import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservationextension.model.aggregate.ReservationExtension;
import com.toolrental.domain.reservationextension.model.valueobject.ReservationExtensionId;
import com.toolrental.domain.reservationextension.port.repository.ReservationExtensionRepository;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.infrastructure.reservationextension.adapters.out.persistence.entity.ReservationExtensionJpaEntity;
import com.toolrental.infrastructure.reservationextension.adapters.out.persistence.repository.ReservationExtensionJpaRepository;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ReservationExtensionPersistenceAdapter implements ReservationExtensionRepository {
  private final ReservationExtensionJpaRepository rows;

  public ReservationExtensionPersistenceAdapter(ReservationExtensionJpaRepository rows) {
    this.rows = rows;
  }

  private ReservationExtension map(ReservationExtensionJpaEntity r) {
    return ReservationExtension.restore(
        new ReservationExtensionId(r.getId()),
        new ReservationId(r.getReservationId()),
        new UserId(r.getRequestedBy()),
        r.getRequestedReturnDate(),
        r.getStatus(),
        r.getRequestedAt(),
        r.getResolvedBy() == null ? null : new UserId(r.getResolvedBy()),
        r.getResolvedAt(),
        new Money(r.getAmount(), Currency.getInstance(r.getCurrency())),
        r.getKey());
  }

  private ReservationExtensionJpaEntity map(ReservationExtension r) {
    return new ReservationExtensionJpaEntity(
        r.id().value(),
        r.reservationId().value(),
        r.requestedBy().value(),
        r.requestedReturnDate(),
        r.status(),
        r.requestedAt(),
        r.resolvedBy() == null ? null : r.resolvedBy().value(),
        r.resolvedAt(),
        r.additionalAmount().amount(),
        r.additionalAmount().currency().getCurrencyCode(),
        r.idempotencyKey());
  }

  public Optional<ReservationExtension> findById(ReservationExtensionId id) {
    return rows.findById(id.value()).map(this::map);
  }

  public Optional<ReservationExtension> findByIdempotencyKey(String key) {
    return rows.findByKey(key).map(this::map);
  }

  public Optional<ReservationExtension> findOpenByReservation(ReservationId id) {
    return rows.findByReservationIdAndStatus(id.value(), "REQUESTED").map(this::map);
  }

  public List<ReservationExtension> findByReservation(ReservationId id) {
    return rows.findByReservationIdOrderByRequestedAt(id.value()).stream().map(this::map).toList();
  }

  public ReservationExtension save(ReservationExtension value) {
    return map(rows.save(map(value)));
  }
}
