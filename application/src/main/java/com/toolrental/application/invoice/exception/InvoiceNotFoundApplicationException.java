package com.toolrental.application.invoice.exception;

import com.toolrental.application.common.exception.ApplicationException;

public final class InvoiceNotFoundApplicationException extends ApplicationException {
  public InvoiceNotFoundApplicationException() {
    super("Invoice not found");
  }
}
