package com.toolrental.infrastructure.payment.adapters.in.scheduling;

import com.toolrental.application.payment.usecase.ReconcileHostedPaymentUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "payments.provider", havingValue = "mercadopago")
public final class PaymentReconciliationScheduler {
  private final ReconcileHostedPaymentUseCase reconcile;

  public PaymentReconciliationScheduler(ReconcileHostedPaymentUseCase reconcile) {
    this.reconcile = reconcile;
  }

  @Scheduled(
      fixedDelayString = "${payments.reconciliation-delay-ms:60000}",
      initialDelayString = "${payments.reconciliation-delay-ms:60000}")
  public void reconcile() {
    reconcile.reconcileDue();
  }
}
