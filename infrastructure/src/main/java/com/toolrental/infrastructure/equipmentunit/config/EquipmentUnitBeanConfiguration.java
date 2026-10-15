package com.toolrental.infrastructure.equipmentunit.config;

import com.toolrental.application.equipmentunit.usecase.RegisterEquipmentUnitUseCase;
import com.toolrental.domain.equipmentunit.port.repository.AtomicEquipmentUnitRegistration;
import com.toolrental.domain.equipmentunit.port.repository.EquipmentUnitRepository;
import com.toolrental.infrastructure.common.config.ClockConfiguration;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import(ClockConfiguration.class)
public class EquipmentUnitBeanConfiguration {
  @Bean
  public RegisterEquipmentUnitUseCase registerEquipmentUnitUseCase(
      AtomicEquipmentUnitRegistration registration, Clock clock) {
    return new RegisterEquipmentUnitUseCase(registration, clock);
  }

  @org.springframework.context.annotation.Bean
  com.toolrental.application.equipmentunit.usecase.ManageEquipmentUnitUseCase
      manageEquipmentUnitUseCase(
          EquipmentUnitRepository repository,
          com.toolrental.domain.common.port.UnitOfWork work,
          com.toolrental.domain.common.port.AccessPolicy access,
          com.toolrental.application.common.service.WorkflowReferences references,
          Clock clock,
          com.toolrental.application.equipmentunit.usecase.RegisterEquipmentUnitUseCase
              registration) {
    return new com.toolrental.application.equipmentunit.usecase.ManageEquipmentUnitUseCase(
        repository, work, access, references, clock, registration);
  }
}
