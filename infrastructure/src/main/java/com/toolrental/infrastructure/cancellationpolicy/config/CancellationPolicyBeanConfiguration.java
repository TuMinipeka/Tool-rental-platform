package com.toolrental.infrastructure.cancellationpolicy.config;

import com.toolrental.application.cancellationpolicy.usecase.ManageCancellationPolicyUseCase;
import com.toolrental.domain.cancellationpolicy.port.repository.CancellationPolicyRepository;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CancellationPolicyBeanConfiguration {
  @Bean
  ManageCancellationPolicyUseCase manageCancellationPolicyUseCase(
      CancellationPolicyRepository policies, AccessPolicy access, UnitOfWork work, Clock clock) {
    return new ManageCancellationPolicyUseCase(policies, access, work, clock);
  }
}
