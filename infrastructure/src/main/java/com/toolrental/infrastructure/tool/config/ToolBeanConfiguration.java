package com.toolrental.infrastructure.tool.config;

import com.toolrental.application.tool.usecase.QueryCatalogUseCase;
import com.toolrental.application.category.usecase.RegisterCategoryUseCase;
import com.toolrental.application.equipmentunit.usecase.RegisterEquipmentUnitUseCase;
import com.toolrental.application.tool.usecase.RegisterToolUseCase;
import com.toolrental.domain.equipmentunit.port.repository.AtomicEquipmentUnitRegistration;
import com.toolrental.domain.category.port.repository.CategoryRepository;
import com.toolrental.domain.equipmentunit.port.repository.EquipmentUnitRepository;
import com.toolrental.domain.tool.port.repository.ToolRepository;
import com.toolrental.domain.common.port.ParticipantsPort;
import com.toolrental.infrastructure.common.config.ClockConfiguration;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import(ClockConfiguration.class)
public class ToolBeanConfiguration {
  @Bean
  @ConditionalOnBean({ToolRepository.class, CategoryRepository.class, ParticipantsPort.class})
  public RegisterToolUseCase registerToolUseCase(
      ToolRepository tools,
      CategoryRepository categories,
      ParticipantsPort participants,
      Clock clock) {
    return new RegisterToolUseCase(tools, categories, participants, clock);
  }
}
