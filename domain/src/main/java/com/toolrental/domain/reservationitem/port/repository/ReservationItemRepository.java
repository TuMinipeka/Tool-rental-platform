package com.toolrental.domain.reservationitem.port.repository;

import com.toolrental.domain.reservationitem.model.aggregate.ReservationItem;
import com.toolrental.domain.reservationitem.model.valueobject.ReservationItemId;
import java.util.List;
import java.util.Optional;

public interface ReservationItemRepository {
  Optional<ReservationItem> findById(ReservationItemId id);

  List<ReservationItem> findAll();

  ReservationItem save(ReservationItem value);

  void delete(ReservationItemId id);
}
