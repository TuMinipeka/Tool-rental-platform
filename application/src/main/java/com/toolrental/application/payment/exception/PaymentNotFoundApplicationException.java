package com.toolrental.application.payment.exception;

import com.toolrental.application.common.exception.ApplicationException;

public final class PaymentNotFoundApplicationException extends ApplicationException {
  public PaymentNotFoundApplicationException() {
    super("Payment not found");
  }
}
