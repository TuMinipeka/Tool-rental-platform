package com.toolrental.application;

import static org.junit.jupiter.api.Assertions.*;

import com.toolrental.application.category.command.RegisterCategoryCommand;
import com.toolrental.application.equipmentunit.command.RegisterEquipmentUnitCommand;
import com.toolrental.application.tool.command.RegisterToolCommand;
import com.toolrental.application.category.exception.CategoryNotFoundApplicationException;
import com.toolrental.application.equipmentunit.exception.EquipmentUnitNotFoundApplicationException;
import com.toolrental.application.supplier.exception.SupplierNotFoundApplicationException;
import com.toolrental.application.tool.usecase.QueryCatalogUseCase;
import com.toolrental.application.category.usecase.RegisterCategoryUseCase;
import com.toolrental.application.equipmentunit.usecase.RegisterEquipmentUnitUseCase;
import com.toolrental.application.tool.usecase.RegisterToolUseCase;
import com.toolrental.application.reservation.command.RequestReservationCommand;
import com.toolrental.application.reservation.dto.ReservationResponse;
import com.toolrental.application.reservation.exception.CustomerNotFoundApplicationException;
import com.toolrental.application.reservation.usecase.CancelReservationUseCase;
import com.toolrental.application.reservation.usecase.ConfirmReservationUseCase;
import com.toolrental.application.reservation.usecase.RejectReservationUseCase;
import com.toolrental.application.reservation.usecase.RequestReservationUseCase;
import com.toolrental.domain.category.model.valueobject.CategoryId;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.valueobject.CustomerId;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.common.model.valueobject.SupplierId;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CatalogAndReservationsTest {
  private final Fixture f = new Fixture();
  private final Clock clock =
      Clock.fixed(Instant.parse("2026-10-14T12:00:00Z"), ZoneId.of("America/Bogota"));
  private final CustomerId customer = CustomerId.generate();
  private final SupplierId supplier = SupplierId.generate();
  private final LocalDate date = LocalDate.of(2026, 10, 14);
  private final RegisterCategoryUseCase registerCategory =
      new RegisterCategoryUseCase(f.categoryRepository, clock);
  private final RegisterToolUseCase registerTool =
      new RegisterToolUseCase(f.toolRepository, f.categoryRepository, f.participants, clock);
  private final RegisterEquipmentUnitUseCase registerEquipmentUnit =
      new RegisterEquipmentUnitUseCase(f.equipmentUnitRegistration, clock);
  private final RequestReservationUseCase request =
      new RequestReservationUseCase(
          f.reservationRepository,
          f.equipmentUnitRepository,
          f.toolRepository,
          f.participants,
          clock);
  private final ConfirmReservationUseCase confirm =
      new ConfirmReservationUseCase(f.management, clock);
  private final CancelReservationUseCase cancel = new CancelReservationUseCase(f.management, clock);
  private final RejectReservationUseCase reject = new RejectReservationUseCase(f.management, clock);
  private final CategoryId category;
  private final ToolId tool;
  private final EquipmentUnitId equipmentUnit;

  CatalogAndReservationsTest() {
    f.customers.add(customer);
    f.suppliers.add(supplier);
    category = registerCategory.execute(new RegisterCategoryCommand("Drilling")).id();
    tool = createTool(supplier);
    equipmentUnit =
        registerEquipmentUnit.execute(new RegisterEquipmentUnitCommand(tool, "T-01")).id();
  }

  private Money money(String value) {
    return new Money(new BigDecimal(value), Currency.getInstance("COP"));
  }

  private ToolId createTool(SupplierId p) {
    return registerTool
        .execute(
            new RegisterToolCommand(
                p, category, "Drill", "Industrial drill", money("10000"), Set.of()))
        .id();
  }

  private ReservationResponse request(EquipmentUnitId... ids) {
    return request.execute(
        new RequestReservationCommand(
            customer, new ReservationPeriod(date, date.plusDays(1)), List.of(ids)));
  }

  @Test
  void registersAndQueriesCatalogWithoutExposingMutableAggregates() {
    var queryCatalog =
        new QueryCatalogUseCase(f.categoryRepository, f.toolRepository, f.equipmentUnitRepository);
    assertEquals("Drilling", queryCatalog.categories().getFirst().name());
    assertEquals(tool, queryCatalog.tools().getFirst().id());
    assertEquals(equipmentUnit, queryCatalog.equipmentUnits(tool).getFirst().id());
    var snapshot = queryCatalog.tool(tool);
    f.tools.get(tool).changeDailyRate(money("50"));
    assertEquals(money("10000"), snapshot.dailyRate());
  }

  @Test
  void validatesReferencesAndInventoryCodeUniquenessPerSupplier() {
    assertThrows(
        BusinessRuleException.class,
        () -> registerCategory.execute(new RegisterCategoryCommand(" Drilling ")));
    assertThrows(
        CategoryNotFoundApplicationException.class,
        () ->
            registerTool.execute(
                new RegisterToolCommand(
                    supplier, CategoryId.generate(), "X", "Y", money("1"), Set.of())));
    assertThrows(
        SupplierNotFoundApplicationException.class, () -> createTool(SupplierId.generate()));
    var otherTool = createTool(supplier);
    assertThrows(
        BusinessRuleException.class,
        () -> registerEquipmentUnit.execute(new RegisterEquipmentUnitCommand(otherTool, "T-01")));
    var otherSupplier = SupplierId.generate();
    f.suppliers.add(otherSupplier);
    var otherSupplierTool = createTool(otherSupplier);
    assertDoesNotThrow(
        () ->
            registerEquipmentUnit.execute(
                new RegisterEquipmentUnitCommand(otherSupplierTool, "T-01")));
  }

  @Test
  void pendingRequestsDoNotBlockAndConflictDoesNotMutateReservation() {
    var first = request(equipmentUnit);
    var second = request(equipmentUnit);
    confirm.execute(first.id(), supplier);
    assertThrows(BusinessRuleException.class, () -> confirm.execute(second.id(), supplier));
    var persisted = f.reservations.get(second.id());
    assertEquals(ReservationStatus.PENDING, persisted.status());
    assertTrue(persisted.confirmation().isEmpty());
  }

  @Test
  void confirmsWithCurrentRateAndFreezesIt() {
    var r = request(equipmentUnit);
    f.tools.get(tool).changeDailyRate(money("15000"));
    var confirmed = confirm.execute(r.id(), supplier);
    assertEquals(money("30000"), confirmed.total());
    f.tools.get(tool).changeDailyRate(money("20000"));
    assertEquals(money("30000"), f.reservations.get(r.id()).total());
    assertThrows(BusinessRuleException.class, () -> confirm.execute(r.id(), supplier));
  }

  @Test
  void reservationSharingFinalDateIsBlockedAndReleaseAllowsConfirmation() {
    var first = request(equipmentUnit);
    confirm.execute(first.id(), supplier);
    var boundary =
        request.execute(
            new RequestReservationCommand(
                customer,
                new ReservationPeriod(date.plusDays(1), date.plusDays(2)),
                List.of(equipmentUnit)));
    assertThrows(BusinessRuleException.class, () -> confirm.execute(boundary.id(), supplier));
    cancel.execute(first.id(), customer);
    assertEquals(ReservationStatus.CONFIRMED, confirm.execute(boundary.id(), supplier).status());
  }

  @Test
  void laterReservationWithoutSharedDateCanBeConfirmed() {
    confirm.execute(request(equipmentUnit).id(), supplier);
    var later =
        request.execute(
            new RequestReservationCommand(
                customer,
                new ReservationPeriod(date.plusDays(2), date.plusDays(2)),
                List.of(equipmentUnit)));
    assertEquals(ReservationStatus.CONFIRMED, confirm.execute(later.id(), supplier).status());
  }

  @Test
  void atomicFailureOnSecondUnitDoesNotPartiallyConfirm() {
    var second = registerEquipmentUnit.execute(new RegisterEquipmentUnitCommand(tool, "T-02")).id();
    confirm.execute(request(second).id(), supplier);
    var multiple = request(equipmentUnit, second);
    assertThrows(BusinessRuleException.class, () -> confirm.execute(multiple.id(), supplier));
    assertEquals(ReservationStatus.PENDING, f.reservations.get(multiple.id()).status());
    assertTrue(f.reservations.get(multiple.id()).confirmation().isEmpty());
    assertDoesNotThrow(() -> confirm.execute(request(equipmentUnit).id(), supplier));
  }

  @Test
  void revalidatesOperationalStatusCurrencyAndProfilesOnConfirmation() {
    var r = request(equipmentUnit);
    f.equipmentUnits.get(equipmentUnit).sendToMaintenance();
    assertThrows(BusinessRuleException.class, () -> confirm.execute(r.id(), supplier));
    f.equipmentUnits.get(equipmentUnit).makeOperational();
    f.tools.get(tool).changeDailyRate(new Money(BigDecimal.ONE, Currency.getInstance("USD")));
    assertThrows(BusinessRuleException.class, () -> confirm.execute(r.id(), supplier));
    f.tools.get(tool).changeDailyRate(money("1"));
    f.customers.remove(customer);
    assertThrows(BusinessRuleException.class, () -> confirm.execute(r.id(), supplier));
    assertEquals(ReservationStatus.PENDING, f.reservations.get(r.id()).status());
  }

  @Test
  void rejectAndCancelValidateActorAndStatus() {
    var r = request(equipmentUnit);
    assertThrows(BusinessRuleException.class, () -> confirm.execute(r.id(), SupplierId.generate()));
    assertThrows(BusinessRuleException.class, () -> cancel.execute(r.id(), CustomerId.generate()));
    assertEquals(ReservationStatus.REJECTED, reject.execute(r.id(), supplier).status());
    assertThrows(BusinessRuleException.class, () -> cancel.execute(r.id(), customer));
    assertThrows(BusinessRuleException.class, () -> confirm.execute(r.id(), supplier));
    var other = request(equipmentUnit);
    cancel.execute(other.id(), customer);
    assertThrows(BusinessRuleException.class, () -> reject.execute(other.id(), supplier));
  }

  @Test
  void requestIsNotSavedWhenValidationFails() {
    int beforeCount = f.reservations.size();
    assertThrows(BusinessRuleException.class, () -> request(equipmentUnit, equipmentUnit));
    assertThrows(
        EquipmentUnitNotFoundApplicationException.class, () -> request(EquipmentUnitId.generate()));
    var p = SupplierId.generate();
    f.suppliers.add(p);
    var u =
        registerEquipmentUnit
            .execute(new RegisterEquipmentUnitCommand(createTool(p), "Other"))
            .id();
    assertThrows(BusinessRuleException.class, () -> request(equipmentUnit, u));
    f.customers.remove(customer);
    assertThrows(CustomerNotFoundApplicationException.class, () -> request(equipmentUnit));
    assertEquals(beforeCount, f.reservations.size());
  }

  @Test
  void atomicPortDiscardsChangesWhenOperationFails() {
    var r = request(equipmentUnit);
    assertThrows(
        IllegalStateException.class,
        () ->
            f.management.execute(
                r.id(),
                context -> {
                  context.reservation().cancel(clock);
                  throw new IllegalStateException("Simulated failure before saving");
                }));
    assertEquals(ReservationStatus.PENDING, f.reservations.get(r.id()).status());
  }
}
