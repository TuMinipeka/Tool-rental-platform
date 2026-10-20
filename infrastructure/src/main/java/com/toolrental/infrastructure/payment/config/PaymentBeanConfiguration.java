package com.toolrental.infrastructure.payment.config;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.invoice.usecase.IssueAcademicInvoiceUseCase;
import com.toolrental.application.payment.usecase.CreateSandboxCheckoutUseCase;
import com.toolrental.application.payment.usecase.ManagePaymentUseCase;
import com.toolrental.application.payment.usecase.ProcessSandboxWebhookUseCase;
import com.toolrental.application.payment.usecase.SimulatePaymentUseCase;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.payment.port.repository.PaymentRepository;
import com.toolrental.domain.rental.port.repository.RentalRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class PaymentBeanConfiguration {
  @Bean
  SimulatePaymentUseCase simulatePaymentUseCase(
      PaymentRepository payments,
      RentalRepository rentals,
      WorkflowReferences references,
      AccessPolicy access,
      UnitOfWork work,
      Clock clock) {
    return new SimulatePaymentUseCase(payments, rentals, references, access, work, clock);
  }

  @Bean
  CreateSandboxCheckoutUseCase createSandboxCheckoutUseCase(
      PaymentRepository payments,
      WorkflowReferences references,
      AccessPolicy access,
      UnitOfWork work,
      Clock clock) {
    return new CreateSandboxCheckoutUseCase(payments, references, access, work, clock);
  }

  @Bean
  ProcessSandboxWebhookUseCase processSandboxWebhookUseCase(
      PaymentRepository payments,
      com.toolrental.domain.reservation.port.repository.AtomicReservationManagement reservations,
      IssueAcademicInvoiceUseCase invoices,
      UnitOfWork work,
      Clock clock) {
    return new ProcessSandboxWebhookUseCase(payments, reservations, invoices, work, clock);
  }

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
