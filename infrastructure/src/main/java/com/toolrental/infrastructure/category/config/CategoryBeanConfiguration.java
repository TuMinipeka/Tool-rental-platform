package com.toolrental.infrastructure.category.config;

import com.toolrental.application.category.usecase.RegisterCategoryUseCase;
import com.toolrental.domain.category.port.repository.CategoryRepository;
import com.toolrental.infrastructure.common.config.ClockConfiguration;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import(ClockConfiguration.class)
public class CategoryBeanConfiguration {
  @Bean
  public RegisterCategoryUseCase registerCategoryUseCase(
      CategoryRepository repository, Clock clock) {
    return new RegisterCategoryUseCase(repository, clock);
  }

  @org.springframework.context.annotation.Bean
  com.toolrental.application.category.usecase.ManageCategoryUseCase manageCategoryUseCase(
      CategoryRepository repository,
      com.toolrental.domain.common.port.UnitOfWork work,
      com.toolrental.domain.common.port.AccessPolicy access,
      com.toolrental.application.common.service.WorkflowReferences references,
      Clock clock,
      com.toolrental.application.category.usecase.RegisterCategoryUseCase registration) {
    return new com.toolrental.application.category.usecase.ManageCategoryUseCase(
        repository, work, access, references, clock, registration);
  }
}
