package com.toolrental.application.refund.usecase;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.payment.exception.PaymentNotFoundApplicationException;
import com.toolrental.application.refund.dto.RefundResponse;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.payment.model.valueobject.PaymentId;
import com.toolrental.domain.payment.port.repository.PaymentRepository;
import com.toolrental.domain.refund.port.repository.RefundRepository;
import java.util.List;
import java.util.UUID;

public final class QueryRefundUseCase {
  private final RefundRepository refunds;
  private final PaymentRepository payments;
  private final WorkflowReferences references;
  private final UnitOfWork work;

  public QueryRefundUseCase(
      RefundRepository refunds,
      PaymentRepository payments,
      WorkflowReferences references,
      UnitOfWork work) {
    this.refunds = refunds;
    this.payments = payments;
    this.references = references;
    this.work = work;
  }

  public List<RefundResponse> byPayment(UUID actor, PaymentId paymentId) {
    return work.execute(
        () -> {
          var payment =
              payments.findById(paymentId).orElseThrow(PaymentNotFoundApplicationException::new);
          references.reservation(actor, payment.reservationId(), false);
          return refunds.findByPayment(paymentId).stream().map(RefundResponse::from).toList();
        });
  }
}
