package com.toolrental.application.payment.usecase;

import com.toolrental.application.common.exception.IdempotencyConflictApplicationException;
import com.toolrental.application.invoice.usecase.IssueAcademicInvoiceUseCase;
import com.toolrental.application.payment.command.ProcessSandboxWebhookCommand;
import com.toolrental.application.payment.dto.PaymentResponse;
import com.toolrental.application.payment.exception.PaymentNotFoundApplicationException;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.payment.port.repository.PaymentRepository;
import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import com.toolrental.domain.reservation.port.repository.AtomicReservationManagement;
import java.time.Clock;

public final class ProcessSandboxWebhookUseCase {
  private final PaymentRepository payments;
  private final AtomicReservationManagement reservations;
  private final IssueAcademicInvoiceUseCase invoices;
  private final UnitOfWork work;
  private final Clock clock;

  public ProcessSandboxWebhookUseCase(
      PaymentRepository payments,
      AtomicReservationManagement reservations,
      IssueAcademicInvoiceUseCase invoices,
      UnitOfWork work,
      Clock clock) {
    this.payments = payments;
    this.reservations = reservations;
    this.invoices = invoices;
    this.work = work;
    this.clock = clock;
  }

  public PaymentResponse execute(ProcessSandboxWebhookCommand command) {
    return work.execute(
        () -> {
          if (!"APPROVED".equals(command.outcome()) && !"DECLINED".equals(command.outcome()))
            throw new BusinessRuleException("Invalid sandbox outcome");
          var payment =
              payments
                  .findByProcessorAndExternalReference(
                      "ACADEMIC_SANDBOX", command.externalReference())
                  .orElseThrow(PaymentNotFoundApplicationException::new);
          var expected = "APPROVED".equals(command.outcome()) ? "CONFIRMED" : "FAILED";
          if (expected.equals(payment.paymentStatus())) return PaymentResponse.from(payment);
          if (!"PENDING".equals(payment.paymentStatus()))
            throw new IdempotencyConflictApplicationException();

          var reservation =
              reservations.execute(
                  payment.reservationId(),
                  context -> {
                    if ("APPROVED".equals(command.outcome()))
                      return context.reservation().confirmPayment(clock);
                    if (context.reservation().status() == ReservationStatus.EXPIRED)
                      return context.reservation();
                    return context.reservation().failPayment(clock);
                  });
          var saved = payments.save(payment.transition(expected, clock));
          if ("CONFIRMED".equals(expected)) invoices.execute(reservation);
          return PaymentResponse.from(saved);
        });
  }
}
