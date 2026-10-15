package com.toolrental.infrastructure.payment.config;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.payment.usecase.ManagePaymentUseCase;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.payment.port.repository.PaymentRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class PaymentBeanConfiguration {
  @Bean
  ManagePaymentUseCase managePaymentUseCase(
      PaymentRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock) {
    return new ManagePaymentUseCase(repository, work, access, references, clock);
  }
}
