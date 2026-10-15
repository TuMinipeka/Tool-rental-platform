package com.toolrental.infrastructure.rental.adapters.out.persistence;

import com.toolrental.domain.rental.port.RentalLifecycle;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class RentalLifecycleAdapter implements RentalLifecycle {
  private final JdbcTemplate db;

  public RentalLifecycleAdapter(JdbcTemplate db) {
    this.db = db;
  }

  public boolean hasDelivery(ReservationId id) {
    return Boolean.TRUE.equals(
        db.queryForObject(
            "select exists(select 1 from unit_deliveries where reservation_id=? and delivered_at is not null)",
            Boolean.class,
            id.value()));
  }

  public boolean allUnitsReturned(ReservationId id) {
    return Boolean.TRUE.equals(
        db.queryForObject(
            "select exists(select 1 from reservation_items where reservation_id=?) and not exists(select 1 from reservation_items i where i.reservation_id=? and not exists(select 1 from unit_returns r where r.reservation_id=i.reservation_id and r.equipment_unit_id=i.equipment_unit_id))",
            Boolean.class,
            id.value(),
            id.value()));
  }
}
