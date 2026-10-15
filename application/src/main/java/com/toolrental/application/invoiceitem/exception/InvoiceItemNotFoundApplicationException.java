package com.toolrental.application.invoiceitem.exception;

import com.toolrental.application.common.exception.ApplicationException;

public final class InvoiceItemNotFoundApplicationException extends ApplicationException {
  public InvoiceItemNotFoundApplicationException() {
    super("InvoiceItem not found");
  }
}
