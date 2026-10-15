package com.toolrental.domain.reservation;

import static org.junit.jupiter.api.Assertions.*;

import com.toolrental.domain.catalog.model.aggregate.Category;
import com.toolrental.domain.catalog.model.aggregate.EquipmentUnit;
import com.toolrental.domain.catalog.model.aggregate.Tool;
import com.toolrental.domain.catalog.model.valueobject.CategoryId;
import com.toolrental.domain.catalog.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.catalog.model.valueobject.ToolId;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.valueobject.CustomerId;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.common.model.valueobject.SupplierId;
import com.toolrental.domain.reservation.model.aggregate.Reservation;
import com.toolrental.domain.reservation.model.entity.ReservationItem;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ReservationTest {
  private final Clock clock = Clock.fixed(Instant.parse("2026-10-14T12:00:00Z"), ZoneOffset.UTC);
  private final SupplierId supplier = SupplierId.generate();
  private final CustomerId customer = CustomerId.generate();
  private final EquipmentUnitId equipmentUnit = EquipmentUnitId.generate();
  private final LocalDate date = LocalDate.of(2026, 10, 14);

  private Money money(String value) {
    return new Money(new BigDecimal(value), Currency.getInstance("COP"));
  }

  private ReservationItem item() {
    return new ReservationItem(equipmentUnit, supplier, money("10000"));
  }

  private Reservation pending() {
    return Reservation.request(customer, new ReservationPeriod(date, date), List.of(item()), clock);
  }

  @Test
  void calculatesInclusiveDaysAndExactTotal() {
    assertEquals(1, new ReservationPeriod(date, date).billableDays());
    assertEquals(2, new ReservationPeriod(date, date.plusDays(1)).billableDays());
    var r =
        Reservation.request(
            customer, new ReservationPeriod(date, date.plusDays(1)), List.of(item()), clock);
    assertEquals(money("20000"), r.total());
    assertEquals(clock.instant(), r.createdAt());
  }

  @Test
  void detectsOverlapAtBothBoundaries() {
    var p = new ReservationPeriod(date, date.plusDays(1));
    assertTrue(p.overlaps(new ReservationPeriod(date.plusDays(1), date.plusDays(2))));
    assertTrue(p.overlaps(new ReservationPeriod(date.minusDays(1), date)));
    assertTrue(p.overlaps(p));
    assertFalse(p.overlaps(new ReservationPeriod(date.plusDays(2), date.plusDays(3))));
  }

  @Test
  void rejectsInvalidDatesAndEmptyReservations() {
    assertThrows(BusinessRuleException.class, () -> new ReservationPeriod(date, date.minusDays(1)));
    assertThrows(
        BusinessRuleException.class,
        () -> Reservation.request(customer, new ReservationPeriod(date, date), List.of(), clock));
  }

  @Test
  void rejectsDuplicatesMixedSuppliersAndCurrencies() {
    var p = new ReservationPeriod(date, date);
    assertThrows(
        BusinessRuleException.class,
        () -> Reservation.request(customer, p, List.of(item(), item()), clock));
    var other = new ReservationItem(EquipmentUnitId.generate(), SupplierId.generate(), money("1"));
    assertThrows(
        BusinessRuleException.class,
        () -> Reservation.request(customer, p, List.of(item(), other), clock));
    var usd =
        new ReservationItem(
            EquipmentUnitId.generate(),
            supplier,
            new Money(BigDecimal.ONE, Currency.getInstance("USD")));
    assertThrows(
        BusinessRuleException.class,
        () -> Reservation.request(customer, p, List.of(item(), usd), clock));
  }

  @Test
  void confirmationFreezesRatesWithoutChangingOriginalRequest() {
    var original = pending();
    var currentItems =
        new ArrayList<>(List.of(new ReservationItem(equipmentUnit, supplier, money("15000"))));
    var confirmed = original.confirmWithRates(currentItems, clock);
    currentItems.clear();
    assertEquals(ReservationStatus.PENDING, original.status());
    assertEquals(ReservationStatus.CONFIRMED, confirmed.status());
    assertEquals(original.id(), confirmed.id());
    assertEquals(money("15000"), confirmed.total());
    assertEquals(clock.instant(), confirmed.confirmation().orElseThrow().occurredOn());
    assertThrows(UnsupportedOperationException.class, () -> confirmed.items().clear());
  }

  @Test
  void confirmationCannotChangeUnitsSupplierOrCurrency() {
    var r = pending();
    assertThrows(
        BusinessRuleException.class,
        () ->
            r.confirmWithRates(
                List.of(new ReservationItem(EquipmentUnitId.generate(), supplier, money("1"))),
                clock));
    assertThrows(
        BusinessRuleException.class,
        () ->
            r.confirmWithRates(
                List.of(new ReservationItem(equipmentUnit, SupplierId.generate(), money("1"))),
                clock));
    assertThrows(
        BusinessRuleException.class,
        () ->
            r.confirmWithRates(
                List.of(
                    new ReservationItem(
                        equipmentUnit,
                        supplier,
                        new Money(BigDecimal.ONE, Currency.getInstance("USD")))),
                clock));
    assertEquals(ReservationStatus.PENDING, r.status());
    assertTrue(r.confirmation().isEmpty());
  }

  @Test
  void rejectAndCancelRespectTerminalStatuses() {
    var rejected = pending();
    rejected.reject(clock);
    assertThrows(BusinessRuleException.class, () -> rejected.cancel(clock));
    assertThrows(BusinessRuleException.class, () -> rejected.reject(clock));
    assertThrows(
        BusinessRuleException.class, () -> rejected.confirmWithRates(List.of(item()), clock));
    var cancelled = pending();
    cancelled.cancel(clock);
    assertThrows(BusinessRuleException.class, () -> cancelled.cancel(clock));
    assertThrows(BusinessRuleException.class, () -> cancelled.reject(clock));
    assertThrows(
        BusinessRuleException.class, () -> cancelled.confirmWithRates(List.of(item()), clock));
    var confirmed = pending().confirmWithRates(List.of(item()), clock);
    assertThrows(BusinessRuleException.class, () -> confirmed.reject(clock));
    assertThrows(
        BusinessRuleException.class, () -> confirmed.confirmWithRates(List.of(item()), clock));
    confirmed.cancel(clock);
    assertEquals(ReservationStatus.CANCELLED, confirmed.status());
  }

  @Test
  void validatesMoneyAndRejectsDifferentCurrencies() {
    assertThrows(BusinessRuleException.class, () -> money("-1"));
    assertThrows(
        BusinessRuleException.class,
        () -> money("1").add(new Money(BigDecimal.ONE, Currency.getInstance("USD"))));
    assertEquals(money("0.3"), money("0.1").add(money("0.2")));
  }

  @Test
  void catalogValidatesNamesImagesAndOperationalStatuses() {
    assertThrows(BusinessRuleException.class, () -> Category.register(" ", clock));
    assertThrows(
        BusinessRuleException.class,
        () ->
            Tool.register(
                supplier,
                CategoryId.generate(),
                "Equipment",
                "Description",
                money("1"),
                Set.of(URI.create("file:///tmp/foto")),
                clock));
    var u = EquipmentUnit.register(ToolId.generate(), "EQ-1", clock);
    u.sendToMaintenance();
    assertFalse(u.isOperational());
    u.makeOperational();
    assertTrue(u.isOperational());
    u.retire();
    assertThrows(BusinessRuleException.class, u::makeOperational);
  }
}
