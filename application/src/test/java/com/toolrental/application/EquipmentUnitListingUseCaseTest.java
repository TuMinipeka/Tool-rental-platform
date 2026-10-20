package com.toolrental.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.toolrental.application.equipmentunit.usecase.ManageEquipmentUnitUseCase;
import com.toolrental.domain.category.model.valueobject.CategoryId;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.equipmentunit.model.aggregate.EquipmentUnit;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.equipmentunit.model.valueobject.OperationalStatus;
import com.toolrental.domain.equipmentunit.port.repository.EquipmentUnitRepository;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import com.toolrental.domain.tool.model.aggregate.Tool;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import com.toolrental.domain.tool.port.repository.ToolRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EquipmentUnitListingUseCaseTest {
  private static final UUID SUPPLIER = UUID.randomUUID();
  private static final UUID OTHER_SUPPLIER = UUID.randomUUID();
  private static final UUID ADMINISTRATOR = UUID.randomUUID();
  private static final ToolId OWN_TOOL_ID = new ToolId(UUID.randomUUID());
  private static final ToolId OTHER_TOOL_ID = new ToolId(UUID.randomUUID());

  @Test
  void supplierListsOnlyUnitsBelongingToOwnedTools() {
    var useCase = useCase();

    var result = useCase.list(SUPPLIER);

    assertEquals(1, result.size());
    assertEquals(OWN_TOOL_ID, result.getFirst().toolId());
  }

  @Test
  void administratorListsEveryUnit() {
    var useCase = useCase();

    var result = useCase.list(ADMINISTRATOR);

    assertEquals(2, result.size());
  }

  private ManageEquipmentUnitUseCase useCase() {
    return new ManageEquipmentUnitUseCase(
        new EquipmentUnits(),
        new DirectUnitOfWork(),
        new ListingAccessPolicy(),
        null,
        Clock.systemUTC(),
        null,
        new Tools());
  }

  private static final class DirectUnitOfWork implements UnitOfWork {
    @Override
    public <T> T execute(java.util.function.Supplier<T> operation) {
      return operation.get();
    }
  }

  private static Tool tool(ToolId id, UUID supplier) {
    return Tool.restore(
        id,
        new SupplierId(supplier),
        new CategoryId(UUID.randomUUID()),
        "Tool",
        "Tool description",
        new Money(new BigDecimal("10000"), Currency.getInstance("COP")),
        Set.of(),
        true);
  }

  private static final class EquipmentUnits implements EquipmentUnitRepository {
    @Override
    public Optional<EquipmentUnit> findById(EquipmentUnitId id) {
      return Optional.empty();
    }

    @Override
    public List<EquipmentUnit> findAll() {
      return List.of(
          EquipmentUnit.restore(
              new EquipmentUnitId(UUID.randomUUID()),
              OWN_TOOL_ID,
              "OWN-001",
              OperationalStatus.OPERATIONAL),
          EquipmentUnit.restore(
              new EquipmentUnitId(UUID.randomUUID()),
              OTHER_TOOL_ID,
              "OTHER-001",
              OperationalStatus.OPERATIONAL));
    }

    @Override
    public void save(EquipmentUnit aggregate) {}
  }

  private static final class Tools implements ToolRepository {
    @Override
    public Optional<Tool> findById(ToolId id) {
      return findAll().stream().filter(tool -> tool.id().equals(id)).findFirst();
    }

    @Override
    public List<Tool> findAll() {
      return List.of(tool(OWN_TOOL_ID, SUPPLIER), tool(OTHER_TOOL_ID, OTHER_SUPPLIER));
    }

    @Override
    public void save(Tool aggregate) {}
  }

  private static final class ListingAccessPolicy implements AccessPolicy {
    @Override
    public void requireActive(UUID actor) {}

    @Override
    public void requireAdministrator(UUID actor) {
      if (!isAdministrator(actor)) throw new IllegalStateException("Administrator required");
    }

    @Override
    public void requireOwnerOrAdministrator(UUID actor, UUID owner) {
      if (!actor.equals(owner) && !isAdministrator(actor))
        throw new IllegalStateException("Owner required");
    }

    @Override
    public void requireRole(UUID actor, String role) {
      if (!"SUPPLIER".equals(role) || !SUPPLIER.equals(actor))
        throw new IllegalStateException("Supplier required");
    }

    @Override
    public boolean isAdministrator(UUID actor) {
      return ADMINISTRATOR.equals(actor);
    }
  }
}
