package com.toolrental.domain.reservation.port.repository;

import com.toolrental.domain.customer.model.valueobject.CustomerId;
import com.toolrental.domain.equipmentunit.model.aggregate.EquipmentUnit;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.aggregate.Reservation;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import com.toolrental.domain.tool.model.aggregate.Tool;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import java.util.function.Function;

/**
 * Executes the operation and saves its result in one transaction. The adapter must reload and lock
 * the reservation and equipment units in a stable order before invoking the operation; every
 * Context read belongs to that transaction. If the operation fails, it saves no changes and
 * publishes no events. The provided reservation must be an isolated copy: mutating the aggregate
 * cannot alter storage before commit. Confirmation, rejection, and cancellation use the same lock
 * to avoid races. Business rules execute in application/domain, not in the adapter.
 */
public interface AtomicReservationManagement {
  Reservation create(Reservation reservation, Function<Context, Reservation> operation);

  Reservation execute(ReservationId id, Function<Context, Reservation> operation);

  interface Context {
    Reservation reservation();

    EquipmentUnit equipmentUnit(EquipmentUnitId id);

    Tool tool(ToolId id);

    boolean isCustomerActive(CustomerId id);

    boolean isSupplierActive(SupplierId id);

    /** Checks active holds and confirmed reservations, with inclusive dates. */
    boolean isOccupied(EquipmentUnitId id, ReservationPeriod period);
  }
}
