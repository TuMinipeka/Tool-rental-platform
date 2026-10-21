package com.toolrental.infrastructure.supplier.config;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.supplier.usecase.ManageSupplierUseCase;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.supplier.port.repository.SupplierRepository;
import com.toolrental.domain.user.port.repository.UserRepository;
import com.toolrental.domain.usersession.port.repository.UserSessionRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SupplierBeanConfiguration {
  @Bean
  ManageSupplierUseCase manageSupplierUseCase(
      SupplierRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      UserRepository users,
      UserSessionRepository sessions) {
    return new ManageSupplierUseCase(repository, work, access, references, clock, users, sessions);
  }
}
