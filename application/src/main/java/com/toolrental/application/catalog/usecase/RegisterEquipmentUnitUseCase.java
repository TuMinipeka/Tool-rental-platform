package com.toolrental.application.catalog.usecase;

import com.toolrental.application.catalog.command.RegisterEquipmentUnitCommand;
import com.toolrental.application.catalog.dto.EquipmentUnitResponse;
import com.toolrental.domain.catalog.port.repository.AtomicEquipmentUnitRegistration;
import java.time.Clock;
import java.util.Objects;

public final class RegisterEquipmentUnitUseCase {
  private final AtomicEquipmentUnitRegistration registration;
  private final Clock clock;

  public RegisterEquipmentUnitUseCase(AtomicEquipmentUnitRegistration registration, Clock clock) {
    this.registration = Objects.requireNonNull(registration);
    this.clock = Objects.requireNonNull(clock);
  }

  public EquipmentUnitResponse execute(RegisterEquipmentUnitCommand c) {
    return EquipmentUnitResponse.from(registration.register(c.toolId(), c.inventoryCode(), clock));
  }
}
