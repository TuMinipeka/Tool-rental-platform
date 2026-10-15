package com.toolrental.infrastructure.reservationitem.adapters.out.persistence;

import com.toolrental.domain.reservationitem.model.aggregate.ReservationItem;
import com.toolrental.domain.reservationitem.model.valueobject.ReservationItemId;
import com.toolrental.domain.reservationitem.port.repository.ReservationItemRepository;
import com.toolrental.infrastructure.reservationitem.adapters.out.persistence.mapper.ReservationItemPersistenceMapper;
import com.toolrental.infrastructure.reservationitem.adapters.out.persistence.repository.ReservationItemJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ReservationItemPersistenceAdapter implements ReservationItemRepository {
  private final ReservationItemJpaRepository rows;
  private final ReservationItemPersistenceMapper mapper;

  public ReservationItemPersistenceAdapter(
      ReservationItemJpaRepository rows, ReservationItemPersistenceMapper mapper) {
    this.rows = rows;
    this.mapper = mapper;
  }

  public Optional<ReservationItem> findById(ReservationItemId id) {
    return rows.findLocked(mapper.key(id)).map(mapper::toDomain);
  }

  public List<ReservationItem> findAll() {
    return rows.findAll().stream().map(mapper::toDomain).toList();
  }

  public ReservationItem save(ReservationItem value) {
    return mapper.toDomain(rows.saveAndFlush(mapper.toEntity(value)));
  }

  public void delete(ReservationItemId id) {
    rows.deleteById(mapper.key(id));
    rows.flush();
  }
}
