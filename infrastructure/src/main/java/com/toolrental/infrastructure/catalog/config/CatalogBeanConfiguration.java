package com.toolrental.infrastructure.catalog.config;

import com.toolrental.application.catalog.usecase.QueryCatalogUseCase;
import com.toolrental.application.catalog.usecase.RegisterCategoryUseCase;
import com.toolrental.application.catalog.usecase.RegisterEquipmentUnitUseCase;
import com.toolrental.application.catalog.usecase.RegisterToolUseCase;
import com.toolrental.domain.catalog.port.repository.AtomicEquipmentUnitRegistration;
import com.toolrental.domain.catalog.port.repository.CategoryRepository;
import com.toolrental.domain.catalog.port.repository.EquipmentUnitRepository;
import com.toolrental.domain.catalog.port.repository.ToolRepository;
import com.toolrental.domain.common.port.ParticipantsPort;
import com.toolrental.infrastructure.common.config.ClockConfiguration;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import(ClockConfiguration.class)
public class CatalogBeanConfiguration {
  @Bean
  @ConditionalOnBean(CategoryRepository.class)
  public RegisterCategoryUseCase registerCategoryUseCase(
      CategoryRepository repository, Clock clock) {
    return new RegisterCategoryUseCase(repository, clock);
  }

  @Bean
  @ConditionalOnBean({ToolRepository.class, CategoryRepository.class, ParticipantsPort.class})
  public RegisterToolUseCase registerToolUseCase(
      ToolRepository tools,
      CategoryRepository categories,
      ParticipantsPort participants,
      Clock clock) {
    return new RegisterToolUseCase(tools, categories, participants, clock);
  }

  @Bean
  @ConditionalOnBean(AtomicEquipmentUnitRegistration.class)
  public RegisterEquipmentUnitUseCase registerEquipmentUnitUseCase(
      AtomicEquipmentUnitRegistration registration, Clock clock) {
    return new RegisterEquipmentUnitUseCase(registration, clock);
  }

  @Bean
  @ConditionalOnBean({
    CategoryRepository.class,
    ToolRepository.class,
    EquipmentUnitRepository.class
  })
  public QueryCatalogUseCase queryCatalogUseCase(
      CategoryRepository categories, ToolRepository tools, EquipmentUnitRepository equipmentUnits) {
    return new QueryCatalogUseCase(categories, tools, equipmentUnits);
  }
}
