package com.toolrental.infrastructure.passwordreset.config;

import com.toolrental.application.passwordreset.usecase.RecoverPasswordUseCase;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.passwordreset.port.PasswordRecoveryNotifier;
import com.toolrental.domain.passwordreset.port.repository.PasswordResetTokenRepository;
import com.toolrental.domain.user.port.PasswordHasher;
import com.toolrental.domain.user.port.SecureTokenService;
import com.toolrental.domain.user.port.repository.UserRepository;
import com.toolrental.domain.usersession.port.repository.UserSessionRepository;
import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class PasswordResetBeanConfiguration {
  @Bean
  RecoverPasswordUseCase recoverPasswordUseCase(
      UserRepository users,
      PasswordHasher passwords,
      PasswordResetTokenRepository resetTokens,
      SecureTokenService secrets,
      PasswordRecoveryNotifier notifier,
      UserSessionRepository sessions,
      UnitOfWork work,
      Clock clock,
      @Value("${auth.password-recovery.ttl:PT15M}") Duration resetTtl) {
    return new RecoverPasswordUseCase(
        users, passwords, resetTokens, secrets, notifier, sessions, work, clock, resetTtl);
  }
}
