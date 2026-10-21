package com.toolrental.application.payment.usecase;

import com.toolrental.application.payment.dto.HostedCheckoutResponse;
import com.toolrental.application.payment.exception.PaymentNotFoundApplicationException;
import com.toolrental.domain.payment.model.valueobject.PaymentId;
import com.toolrental.domain.payment.port.repository.HostedCheckoutRepository;
import java.util.UUID;

public final class GetHostedCheckoutUseCase {
  private final ManagePaymentUseCase payments;
  private final HostedCheckoutRepository states;

  public GetHostedCheckoutUseCase(ManagePaymentUseCase payments, HostedCheckoutRepository states) {
    this.payments = payments;
    this.states = states;
  }

  public HostedCheckoutResponse execute(UUID actor, UUID id) {
    payments.find(actor, new PaymentId(id));
    return HostedCheckoutResponse.from(
        states.find(id).orElseThrow(PaymentNotFoundApplicationException::new));
  }
}
