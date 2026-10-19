package com.toolrental.infrastructure.tool.config;

import com.toolrental.application.tool.usecase.BrowseCatalogUseCase;
import com.toolrental.application.tool.usecase.CheckToolAvailabilityUseCase;
import com.toolrental.application.tool.usecase.RegisterToolUseCase;
import com.toolrental.domain.category.port.repository.CategoryRepository;
import com.toolrental.domain.common.port.ParticipantsPort;
import com.toolrental.domain.tool.port.repository.ToolRepository;
import com.toolrental.infrastructure.common.config.ClockConfiguration;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import(ClockConfiguration.class)
public class ToolBeanConfiguration {
  @Bean
  BrowseCatalogUseCase browseCatalogUseCase(
      ToolRepository tools,
      CategoryRepository categories,
      com.toolrental.domain.equipmentunit.port.repository.EquipmentUnitRepository units,
      com.toolrental.domain.common.port.UnitOfWork work) {
    return new BrowseCatalogUseCase(tools, categories, units, work);
  }

  @Bean
  CheckToolAvailabilityUseCase checkToolAvailabilityUseCase(
      ToolRepository tools,
      com.toolrental.domain.reservation.port.repository.AvailabilityRepository availability,
      com.toolrental.domain.common.port.UnitOfWork work) {
    return new CheckToolAvailabilityUseCase(tools, availability, work);
  }

  @Bean
  public RegisterToolUseCase registerToolUseCase(
      ToolRepository tools,
      CategoryRepository categories,
      ParticipantsPort participants,
      Clock clock) {
    return new RegisterToolUseCase(tools, categories, participants, clock);
  }

  @org.springframework.context.annotation.Bean
  com.toolrental.application.tool.usecase.ManageToolUseCase manageToolUseCase(
      ToolRepository repository,
      com.toolrental.domain.common.port.UnitOfWork work,
      com.toolrental.domain.common.port.AccessPolicy access,
      com.toolrental.application.common.service.WorkflowReferences references,
      Clock clock,
      com.toolrental.application.tool.usecase.RegisterToolUseCase registration) {
    return new com.toolrental.application.tool.usecase.ManageToolUseCase(
        repository, work, access, references, clock, registration);
  }
}
