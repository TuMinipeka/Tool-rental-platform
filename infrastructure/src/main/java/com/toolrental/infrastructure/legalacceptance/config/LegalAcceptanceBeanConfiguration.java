package com.toolrental.infrastructure.legalacceptance.config;

import com.toolrental.application.legalacceptance.usecase.ManageLegalAcceptanceUseCase;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.legalacceptance.port.repository.LegalAcceptanceRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class LegalAcceptanceBeanConfiguration {
  @Bean
  ManageLegalAcceptanceUseCase manageLegalAcceptanceUseCase(
      LegalAcceptanceRepository repository, AccessPolicy access, UnitOfWork work, Clock clock) {
    return new ManageLegalAcceptanceUseCase(repository, access, work, clock);
  }
}
