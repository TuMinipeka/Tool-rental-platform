package com.toolrental.domain.common;

import static org.junit.jupiter.api.Assertions.*;

import com.toolrental.domain.category.event.CategoryRegistered;
import com.toolrental.domain.equipmentunit.event.EquipmentUnitRegistered;
import com.toolrental.domain.tool.event.ToolRegistered;
import com.toolrental.domain.category.model.aggregate.Category;
import com.toolrental.domain.equipmentunit.model.aggregate.EquipmentUnit;
import com.toolrental.domain.tool.model.aggregate.Tool;
import com.toolrental.domain.common.model.valueobject.CustomerId;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.common.model.valueobject.SupplierId;
import com.toolrental.domain.reservation.event.ReservationCancelled;
import com.toolrental.domain.reservation.event.ReservationConfirmed;
import com.toolrental.domain.reservation.event.ReservationRejected;
import com.toolrental.domain.reservation.event.ReservationRequested;
import com.toolrental.domain.reservation.model.aggregate.Reservation;
import com.toolrental.domain.reservationitem.model.entity.ReservationItem;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AggregateEventsTest {
  private final Clock clock = Clock.fixed(Instant.parse("2026-10-14T12:00:00Z"), ZoneOffset.UTC);

  @Test
  void recordsTypedEventsWithInjectedClock() {
    var category = Category.register("Drilling", clock);
    var tool =
        Tool.register(
            SupplierId.generate(),
            category.id(),
            "Drill",
            "Industrial",
            new Money(BigDecimal.TEN, Currency.getInstance("COP")),
            Set.of(),
            clock);
    var equipmentUnit = EquipmentUnit.register(tool.id(), "EQ-1", clock);
    assertInstanceOf(CategoryRegistered.class, category.domainEvents().getFirst());
    assertInstanceOf(ToolRegistered.class, tool.domainEvents().getFirst());
    assertInstanceOf(EquipmentUnitRegistered.class, equipmentUnit.domainEvents().getFirst());
    assertEquals(clock.instant(), tool.domainEvents().getFirst().occurredOn());
    assertEquals(clock.instant(), equipmentUnit.domainEvents().getFirst().occurredOn());
  }

  @Test
  void eventsAreAnImmutableSnapshotThatCanBeCleared() {
    var category = Category.register("Catalog", clock);
    var events = category.domainEvents();
    assertThrows(UnsupportedOperationException.class, events::clear);
    category.clearDomainEvents();
    assertTrue(category.domainEvents().isEmpty());
    assertEquals(1, events.size());
  }

  @Test
  void restoringCategoryDoesNotEmitRegistrationEvent() {
    var category = Category.register("Catalog", clock);
    var restored = Category.restore(category.id(), category.name());
    assertEquals(category.id(), restored.id());
    assertTrue(restored.domainEvents().isEmpty());
  }

  @Test
  void reservationCopyDoesNotRepeatEventsAndConfirmationRecordsItsTransition() {
    var pending = request();
    assertInstanceOf(ReservationRequested.class, pending.domainEvents().getFirst());
    assertTrue(pending.copy().domainEvents().isEmpty());
    var confirmed = pending.confirmWithRates(pending.items(), clock);
    assertEquals(1, confirmed.domainEvents().size());
    assertInstanceOf(ReservationConfirmed.class, confirmed.domainEvents().getFirst());
    assertEquals(confirmed.confirmation().orElseThrow(), confirmed.domainEvents().getFirst());
    confirmed.clearDomainEvents();
    assertTrue(confirmed.confirmation().isPresent());
  }

  @Test
  void rejectAndCancelRecordTransitionsAndFailureAddsNoEvents() {
    var rejected = request();
    rejected.clearDomainEvents();
    rejected.reject(clock);
    assertInstanceOf(ReservationRejected.class, rejected.domainEvents().getFirst());
    assertThrows(
        com.toolrental.domain.common.exception.BusinessRuleException.class,
        () -> rejected.cancel(clock));
    assertEquals(1, rejected.domainEvents().size());
    var cancelled = request();
    cancelled.clearDomainEvents();
    cancelled.cancel(clock);
    assertInstanceOf(ReservationCancelled.class, cancelled.domainEvents().getFirst());
  }

  @Test
  void eventRejectsNullIdentityAndTimestamp() {
    assertThrows(NullPointerException.class, () -> new CategoryRegistered(null, clock.instant()));
    var category = Category.register("Catalog", clock);
    assertThrows(NullPointerException.class, () -> new CategoryRegistered(category.id(), null));
  }

  @Test
  void restoringToolAndUnitPreservesIdentityAndStatusWithoutNewEvents() {
    var category = Category.register("Catalog", clock);
    var tool =
        Tool.register(
            SupplierId.generate(),
            category.id(),
            "Drill",
            "Industrial",
            new Money(BigDecimal.TEN, Currency.getInstance("COP")),
            Set.of(),
            clock);
    tool.unpublish();
    var restored =
        Tool.restore(
            tool.id(),
            tool.supplierId(),
            tool.categoryId(),
            tool.name(),
            tool.description(),
            tool.dailyRate(),
            tool.imageUrls(),
            tool.published());
    assertEquals(tool.id(), restored.id());
    assertFalse(restored.published());
    assertTrue(restored.domainEvents().isEmpty());
    var equipmentUnit = EquipmentUnit.register(tool.id(), "EQ-1", clock);
    equipmentUnit.sendToMaintenance();
    var restoredEquipmentUnit =
        EquipmentUnit.restore(
            equipmentUnit.id(),
            equipmentUnit.toolId(),
            equipmentUnit.inventoryCode(),
            equipmentUnit.status());
    assertEquals(equipmentUnit.id(), restoredEquipmentUnit.id());
    assertFalse(restoredEquipmentUnit.isOperational());
    assertTrue(restoredEquipmentUnit.domainEvents().isEmpty());
  }

  @Test
  void restoringReservationPreservesConfirmationWithoutReemittingEvents() {
    var pending = request();
    var confirmed = pending.confirmWithRates(pending.items(), clock);
    var restored =
        Reservation.restore(
            confirmed.id(),
            confirmed.customerId(),
            confirmed.period(),
            confirmed.items(),
            confirmed.createdAt(),
            confirmed.status(),
            confirmed.confirmation().orElseThrow().occurredOn());
    assertEquals(confirmed.id(), restored.id());
    assertEquals(confirmed.status(), restored.status());
    assertEquals(confirmed.total(), restored.total());
    assertEquals(confirmed.confirmation(), restored.confirmation());
    assertTrue(restored.domainEvents().isEmpty());
    assertThrows(
        com.toolrental.domain.common.exception.BusinessRuleException.class,
        () ->
            Reservation.restore(
                confirmed.id(),
                confirmed.customerId(),
                confirmed.period(),
                confirmed.items(),
                confirmed.createdAt(),
                confirmed.status(),
                null));
  }

  private Reservation request() {
    var category = Category.register("Catalog", clock);
    var supplier = SupplierId.generate();
    var rate = new Money(BigDecimal.TEN, Currency.getInstance("COP"));
    var tool = Tool.register(supplier, category.id(), "Drill", "Industrial", rate, Set.of(), clock);
    var equipmentUnit = EquipmentUnit.register(tool.id(), "EQ-1", clock);
    var date = LocalDate.of(2026, 10, 14);
    return Reservation.request(
        CustomerId.generate(),
        new ReservationPeriod(date, date),
        List.of(new ReservationItem(equipmentUnit.id(), supplier, rate)),
        clock);
  }
}
