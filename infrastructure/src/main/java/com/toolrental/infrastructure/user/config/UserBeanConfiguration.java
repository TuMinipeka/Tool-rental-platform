package com.toolrental.infrastructure.user.config;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.user.usecase.AuthenticateUserUseCase;
import com.toolrental.application.user.usecase.ManageUserUseCase;
import com.toolrental.application.user.usecase.RegisterCustomerAccountUseCase;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.customer.port.repository.CustomerRepository;
import com.toolrental.domain.role.port.repository.RoleRepository;
import com.toolrental.domain.user.port.AuthenticationTokenProvider;
import com.toolrental.domain.user.port.PasswordHasher;
import com.toolrental.domain.user.port.SecureTokenService;
import com.toolrental.domain.user.port.UserRoleReader;
import com.toolrental.domain.user.port.repository.UserRepository;
import com.toolrental.domain.userrole.port.repository.UserRoleRepository;
import com.toolrental.domain.usersession.port.repository.UserSessionRepository;
import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
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
      Clock clock,
      UserSessionRepository sessions,
      SecureTokenService secrets,
      @Value("${auth.jwt.refresh-ttl:P30D}") Duration refreshTtl) {
    return new AuthenticateUserUseCase(
        users, passwords, roles, tokens, work, clock, sessions, secrets, refreshTtl);
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
      Clock clock,
      UserSessionRepository sessions,
      SecureTokenService secrets,
      @Value("${auth.jwt.refresh-ttl:P30D}") Duration refreshTtl) {
    return new RegisterCustomerAccountUseCase(
        users,
        customers,
        roles,
        assignments,
        passwords,
        tokens,
        work,
        clock,
        sessions,
        secrets,
        refreshTtl);
  }

  @Bean
  ManageUserUseCase manageUserUseCase(
      UserRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      PasswordHasher hasher,
      UserSessionRepository sessions) {
    return new ManageUserUseCase(repository, work, access, references, clock, hasher, sessions);
  }
}
