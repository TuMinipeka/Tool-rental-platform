package com.toolrental.infrastructure.usersession.config;

import com.toolrental.application.usersession.usecase.ManageUserSessionUseCase;
import com.toolrental.application.usersession.usecase.RefreshSessionUseCase;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.user.port.AuthenticationTokenProvider;
import com.toolrental.domain.user.port.SecureTokenService;
import com.toolrental.domain.user.port.UserRoleReader;
import com.toolrental.domain.user.port.repository.UserRepository;
import com.toolrental.domain.usersession.port.repository.UserSessionRepository;
import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class UserSessionBeanConfiguration {
  @Bean
  RefreshSessionUseCase refreshSessionUseCase(
      UserSessionRepository sessions,
      UserRepository users,
      UserRoleReader roles,
      SecureTokenService secrets,
      AuthenticationTokenProvider tokens,
      UnitOfWork work,
      Clock clock,
      @Value("${auth.jwt.refresh-ttl:P30D}") Duration refreshTtl,
      @Value("${auth.jwt.refresh-reuse-grace:PT10S}") Duration reuseGrace) {
    return new RefreshSessionUseCase(
        sessions, users, roles, secrets, tokens, work, clock, refreshTtl, reuseGrace);
  }

  @Bean
  ManageUserSessionUseCase manageUserSessionUseCase(
      UserSessionRepository sessions, SecureTokenService secrets, UnitOfWork work, Clock clock) {
    return new ManageUserSessionUseCase(sessions, secrets, work, clock);
  }
}
