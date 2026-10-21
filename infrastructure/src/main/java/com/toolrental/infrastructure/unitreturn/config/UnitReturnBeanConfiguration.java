package com.toolrental.infrastructure.unitreturn.config;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.unitreturn.usecase.ManageUnitReturnUseCase;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.rental.port.repository.RentalRepository;
import com.toolrental.domain.unitdelivery.port.repository.UnitDeliveryRepository;
import com.toolrental.domain.unitreturn.port.repository.UnitReturnRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class UnitReturnBeanConfiguration {
  @Bean
  ManageUnitReturnUseCase manageUnitReturnUseCase(
      UnitReturnRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      UnitDeliveryRepository deliveries,
      RentalRepository rentals,
      com.toolrental.domain.payment.port.repository.PaymentRepository payments) {
    return new ManageUnitReturnUseCase(
        repository, work, access, references, clock, deliveries, rentals, payments);
  }
}
