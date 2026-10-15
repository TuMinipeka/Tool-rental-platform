package com.toolrental.infrastructure.user.config;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.user.usecase.ManageUserUseCase;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.user.port.PasswordHasher;
import com.toolrental.domain.user.port.repository.UserRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class UserBeanConfiguration {
  @Bean
  ManageUserUseCase manageUserUseCase(
      UserRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      PasswordHasher hasher) {
    return new ManageUserUseCase(repository, work, access, references, clock, hasher);
  }
}
