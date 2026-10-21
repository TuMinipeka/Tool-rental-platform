package com.toolrental.infrastructure.refund.config;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.refund.usecase.QueryRefundUseCase;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.payment.port.repository.PaymentRepository;
import com.toolrental.domain.refund.port.repository.RefundRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class RefundBeanConfiguration {
  @Bean
  QueryRefundUseCase queryRefundUseCase(
      RefundRepository refunds,
      PaymentRepository payments,
      WorkflowReferences references,
      UnitOfWork work) {
    return new QueryRefundUseCase(refunds, payments, references, work);
  }
}
