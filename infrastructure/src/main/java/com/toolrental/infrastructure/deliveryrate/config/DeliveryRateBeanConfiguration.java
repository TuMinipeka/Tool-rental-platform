package com.toolrental.infrastructure.deliveryrate.config;

import com.toolrental.application.deliveryrate.usecase.ManageDeliveryRateUseCase;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.deliveryrate.port.repository.DeliveryRateRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class DeliveryRateBeanConfiguration {
  @Bean
  ManageDeliveryRateUseCase manageDeliveryRateUseCase(
      DeliveryRateRepository rates, AccessPolicy access, UnitOfWork work, Clock clock) {
    return new ManageDeliveryRateUseCase(rates, access, work, clock);
  }
}
