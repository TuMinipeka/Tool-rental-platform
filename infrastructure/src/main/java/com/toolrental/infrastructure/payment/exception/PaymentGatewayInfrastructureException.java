package com.toolrental.infrastructure.payment.exception;

import com.toolrental.infrastructure.common.exception.InfrastructureException;

public final class PaymentGatewayInfrastructureException extends InfrastructureException {
  public PaymentGatewayInfrastructureException() {
    super("Payment provider is temporarily unavailable; checkout requires reconciliation");
  }
}
