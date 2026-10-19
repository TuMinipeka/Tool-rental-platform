package com.toolrental.infrastructure.user.config;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.user.usecase.AuthenticateUserUseCase;
import com.toolrental.application.user.usecase.ManageUserUseCase;
import com.toolrental.application.user.usecase.RecoverPasswordUseCase;
import com.toolrental.application.user.usecase.RegisterCustomerAccountUseCase;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.customer.port.repository.CustomerRepository;
import com.toolrental.domain.role.port.repository.RoleRepository;
import com.toolrental.domain.user.port.AuthenticationTokenProvider;
import com.toolrental.domain.user.port.PasswordHasher;
import com.toolrental.domain.user.port.UserRoleReader;
import com.toolrental.domain.user.port.repository.UserRepository;
import com.toolrental.domain.userrole.port.repository.UserRoleRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class UserBeanConfiguration {
  @Bean
  AuthenticateUserUseCase authenticateUserUseCase(
      UserRepository users,
      PasswordHasher passwords,
      UserRoleReader roles,
      AuthenticationTokenProvider tokens,
      UnitOfWork work,
      Clock clock) {
    return new AuthenticateUserUseCase(users, passwords, roles, tokens, work, clock);
  }

  @Bean
  RegisterCustomerAccountUseCase registerCustomerAccountUseCase(
      UserRepository users,
      CustomerRepository customers,
      RoleRepository roles,
      UserRoleRepository assignments,
      PasswordHasher passwords,
      AuthenticationTokenProvider tokens,
      UnitOfWork work,
      Clock clock) {
    return new RegisterCustomerAccountUseCase(
        users, customers, roles, assignments, passwords, tokens, work, clock);
  }

  @Bean
  RecoverPasswordUseCase recoverPasswordUseCase(
      UserRepository users,
      PasswordHasher passwords,
      AuthenticationTokenProvider tokens,
      UnitOfWork work,
      Clock clock) {
    return new RecoverPasswordUseCase(users, passwords, tokens, work, clock);
  }

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
