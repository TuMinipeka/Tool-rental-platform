package com.toolrental.application.equipmentunit.usecase;

import com.toolrental.application.equipmentunit.command.RegisterEquipmentUnitCommand;
import com.toolrental.application.equipmentunit.dto.EquipmentUnitResponse;
import com.toolrental.domain.equipmentunit.port.repository.AtomicEquipmentUnitRegistration;
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
