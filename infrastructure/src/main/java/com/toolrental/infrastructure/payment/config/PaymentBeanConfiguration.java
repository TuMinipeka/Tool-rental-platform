package com.toolrental.infrastructure.payment.config;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.invoice.usecase.IssueAcademicInvoiceUseCase;
import com.toolrental.application.payment.usecase.CreateHostedCheckoutUseCase;
import com.toolrental.application.payment.usecase.CreateSandboxCheckoutUseCase;
import com.toolrental.application.payment.usecase.GetHostedCheckoutUseCase;
import com.toolrental.application.payment.usecase.ManagePaymentUseCase;
import com.toolrental.application.payment.usecase.OperationalReportUseCase;
import com.toolrental.application.payment.usecase.ProcessSandboxWebhookUseCase;
import com.toolrental.application.payment.usecase.ReconcileHostedPaymentUseCase;
import com.toolrental.application.payment.usecase.SimulatePaymentUseCase;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.payment.port.OperationalReportingPort;
import com.toolrental.domain.payment.port.PaymentGateway;
import com.toolrental.domain.payment.port.repository.HostedCheckoutRepository;
import com.toolrental.domain.payment.port.repository.PaymentRepository;
import com.toolrental.domain.refund.port.repository.RefundRepository;
import com.toolrental.domain.rental.port.repository.RentalRepository;
import com.toolrental.domain.reservation.port.repository.AtomicReservationManagement;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class PaymentBeanConfiguration {
  @Bean
  GetHostedCheckoutUseCase getHostedCheckoutUseCase(
      ManagePaymentUseCase payments, HostedCheckoutRepository states) {
    return new GetHostedCheckoutUseCase(payments, states);
  }

  @Bean
  ReconcileHostedPaymentUseCase reconcileHostedPaymentUseCase(
      PaymentGateway gateway,
      HostedCheckoutRepository checkouts,
      PaymentRepository payments,
      AtomicReservationManagement reservations,
      IssueAcademicInvoiceUseCase invoices,
      UnitOfWork work,
      Clock clock,
      @org.springframework.beans.factory.annotation.Value("${payments.mercadopago.collector-id:}")
          String collector,
      RefundRepository refunds) {
    return new ReconcileHostedPaymentUseCase(
        gateway, checkouts, payments, reservations, invoices, work, clock, collector, refunds);
  }

  @Bean
  CreateHostedCheckoutUseCase createHostedCheckoutUseCase(
      PaymentRepository payments,
      HostedCheckoutRepository checkouts,
      WorkflowReferences references,
      AccessPolicy access,
      UnitOfWork work,
      AtomicReservationManagement reservations,
      ReconcileHostedPaymentUseCase reconcile,
      Clock clock) {
    return new CreateHostedCheckoutUseCase(
        payments, checkouts, references, access, work, reservations, reconcile, clock);
  }

  @Bean
  OperationalReportUseCase operationalReportUseCase(
      OperationalReportingPort reports, AccessPolicy access, UnitOfWork work, Clock clock) {
    return new OperationalReportUseCase(reports, access, work, clock);
  }

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
      AtomicReservationManagement reservations,
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
