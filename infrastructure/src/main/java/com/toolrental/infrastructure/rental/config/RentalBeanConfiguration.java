package com.toolrental.infrastructure.rental.config;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.rental.usecase.ManageRentalUseCase;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.rental.port.repository.RentalRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class RentalBeanConfiguration {
  @Bean
  ManageRentalUseCase manageRentalUseCase(
      RentalRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      com.toolrental.domain.rental.port.RentalLifecycle lifecycle) {
    return new ManageRentalUseCase(repository, work, access, references, clock, lifecycle);
  }
}
