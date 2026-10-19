package com.toolrental.application;

import com.toolrental.application.equipmentunit.exception.EquipmentUnitNotFoundApplicationException;
import com.toolrental.application.reservation.exception.ReservationNotFoundApplicationException;
import com.toolrental.application.tool.exception.ToolNotFoundApplicationException;
import com.toolrental.domain.category.model.aggregate.Category;
import com.toolrental.domain.category.model.valueobject.CategoryId;
import com.toolrental.domain.category.port.repository.CategoryRepository;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.port.ParticipantsPort;
import com.toolrental.domain.customer.model.valueobject.CustomerId;
import com.toolrental.domain.equipmentunit.model.aggregate.EquipmentUnit;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.equipmentunit.port.repository.AtomicEquipmentUnitRegistration;
import com.toolrental.domain.equipmentunit.port.repository.EquipmentUnitRepository;
import com.toolrental.domain.reservation.model.aggregate.Reservation;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import com.toolrental.domain.reservation.port.repository.AtomicReservationManagement;
import com.toolrental.domain.reservation.port.repository.ReservationRepository;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import com.toolrental.domain.tool.model.aggregate.Tool;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import com.toolrental.domain.tool.port.repository.ToolRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Test-only in-memory adapters; they do not prove PostgreSQL transactions or locking. */
final class Fixture {
  final Map<CategoryId, Category> categories = new HashMap<>();
  final Map<ToolId, Tool> tools = new HashMap<>();
  final Map<EquipmentUnitId, EquipmentUnit> equipmentUnits = new HashMap<>();
  final Map<ReservationId, Reservation> reservations = new HashMap<>();
  final Set<CustomerId> customers = new HashSet<>();
  final Set<SupplierId> suppliers = new HashSet<>();

  final ParticipantsPort participants =
      new ParticipantsPort() {
        public boolean isCustomerActive(CustomerId id) {
          return customers.contains(id);
        }

        public boolean isSupplierActive(SupplierId id) {
          return suppliers.contains(id);
        }
      };
  final CategoryRepository categoryRepository =
      new CategoryRepository() {
        public Optional<Category> findById(CategoryId id) {
          return Optional.ofNullable(categories.get(id));
        }

        public List<Category> findAll() {
          return List.copyOf(categories.values());
        }

        public void delete(CategoryId id) {
          categories.remove(id);
        }

        public void save(Category c) {
          categories.put(c.id(), c);
        }
      };
  final ToolRepository toolRepository =
      new ToolRepository() {
        public Optional<Tool> findById(ToolId id) {
          return Optional.ofNullable(tools.get(id));
        }

        public List<Tool> findAll() {
          return List.copyOf(tools.values());
        }

        public void save(Tool h) {
          tools.put(h.id(), h);
        }
      };
  final EquipmentUnitRepository equipmentUnitRepository =
      new EquipmentUnitRepository() {
        public Optional<EquipmentUnit> findById(EquipmentUnitId id) {
          return Optional.ofNullable(equipmentUnits.get(id));
        }

        public List<EquipmentUnit> findAll() {
          return List.copyOf(equipmentUnits.values());
        }

        public void save(EquipmentUnit u) {
          equipmentUnits.put(u.id(), u);
        }
      };
  final ReservationRepository reservationRepository =
      new ReservationRepository() {
        public Optional<Reservation> findById(ReservationId id) {
          return Optional.ofNullable(reservations.get(id));
        }

        public List<Reservation> findByCustomer(CustomerId customerId) {
          return reservations.values().stream()
              .filter(reservation -> reservation.customerId().equals(customerId))
              .toList();
        }

        public List<Reservation> findBySupplier(SupplierId supplierId) {
          return reservations.values().stream()
              .filter(reservation -> reservation.supplierId().equals(supplierId))
              .toList();
        }

        public void addRequest(Reservation r) {
          if (r.status() != ReservationStatus.PENDING || reservations.containsKey(r.id()))
            throw new BusinessRuleException("Invalid reservation request");
          reservations.put(r.id(), r);
        }
      };
  final AtomicEquipmentUnitRegistration equipmentUnitRegistration =
      (id, code, clock) -> {
        synchronized (this) {
          var h = tool(id);
          var equipmentUnit = EquipmentUnit.register(id, code, clock);
          boolean duplicate =
              equipmentUnits.values().stream()
                  .anyMatch(
                      u ->
                          u.inventoryCode().equals(equipmentUnit.inventoryCode())
                              && tool(u.toolId()).supplierId().equals(h.supplierId()));
          if (duplicate)
            throw new BusinessRuleException("Inventory code must be unique per supplier");
          equipmentUnits.put(equipmentUnit.id(), equipmentUnit);
          return equipmentUnit;
        }
      };
  final AtomicReservationManagement management =
      (id, operation) -> {
        synchronized (this) {
          var isolated = reservation(id).copy();
          var context =
              new AtomicReservationManagement.Context() {
                public Reservation reservation() {
                  return isolated;
                }

                public EquipmentUnit equipmentUnit(EquipmentUnitId equipmentUnitId) {
                  return equipmentUnitRepository
                      .findById(equipmentUnitId)
                      .orElseThrow(
                          () -> new EquipmentUnitNotFoundApplicationException(equipmentUnitId));
                }

                public Tool tool(ToolId toolId) {
                  return Fixture.this.tool(toolId);
                }

                public boolean isCustomerActive(CustomerId customer) {
                  return participants.isCustomerActive(customer);
                }

                public boolean isSupplierActive(SupplierId supplier) {
                  return participants.isSupplierActive(supplier);
                }

                public boolean isOccupied(
                    EquipmentUnitId equipmentUnitId, ReservationPeriod period) {
                  return reservations.values().stream()
                      .filter(other -> !other.id().equals(id))
                      .filter(other -> other.status() == ReservationStatus.CONFIRMED)
                      .filter(other -> other.period().overlaps(period))
                      .anyMatch(
                          other ->
                              other.items().stream()
                                  .anyMatch(d -> d.equipmentUnitId().equals(equipmentUnitId)));
                }
              };
          var result = operation.apply(context);
          if (!id.equals(result.id()))
            throw new BusinessRuleException("Reservation identity cannot change");
          reservations.put(id, result);
          return result;
        }
      };

  private Tool tool(ToolId id) {
    return Optional.ofNullable(tools.get(id))
        .orElseThrow(() -> new ToolNotFoundApplicationException(id));
  }

  private Reservation reservation(ReservationId id) {
    return reservationRepository
        .findById(id)
        .orElseThrow(() -> new ReservationNotFoundApplicationException(id));
  }
}
