package com.toolrental.infrastructure.damagereport.config;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.damagereport.usecase.ManageDamageReportUseCase;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.damagereport.port.repository.DamageReportRepository;
import com.toolrental.domain.unitreturn.port.repository.UnitReturnRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class DamageReportBeanConfiguration {
  @Bean
  ManageDamageReportUseCase manageDamageReportUseCase(
      DamageReportRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      UnitReturnRepository returns) {
    return new ManageDamageReportUseCase(repository, work, access, references, clock, returns);
  }
}
