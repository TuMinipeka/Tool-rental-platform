package com.toolrental.infrastructure.unitdelivery.config;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.unitdelivery.usecase.ManageUnitDeliveryUseCase;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.rental.port.repository.RentalRepository;
import com.toolrental.domain.unitdelivery.port.repository.UnitDeliveryRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class UnitDeliveryBeanConfiguration {
  @Bean
  ManageUnitDeliveryUseCase manageUnitDeliveryUseCase(
      UnitDeliveryRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      RentalRepository rentals) {
    return new ManageUnitDeliveryUseCase(repository, work, access, references, clock, rentals);
  }
}
