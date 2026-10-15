package com.toolrental.infrastructure.customer.config;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.customer.usecase.ManageCustomerUseCase;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.customer.port.repository.CustomerRepository;
import com.toolrental.domain.user.port.repository.UserRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CustomerBeanConfiguration {
  @Bean
  ManageCustomerUseCase manageCustomerUseCase(
      CustomerRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      UserRepository users) {
    return new ManageCustomerUseCase(repository, work, access, references, clock, users);
  }
}
