package com.toolrental.application.damagereport.exception;

import com.toolrental.application.common.exception.ApplicationException;

public final class DamageReportNotFoundApplicationException extends ApplicationException {
  public DamageReportNotFoundApplicationException() {
    super("DamageReport not found");
  }
}
