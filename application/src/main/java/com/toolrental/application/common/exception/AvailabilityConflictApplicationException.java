package com.toolrental.application.common.exception;

public final class AvailabilityConflictApplicationException extends ApplicationException {
  public AvailabilityConflictApplicationException() {
    super("Equipment unit is unavailable for the requested period");
  }
}
