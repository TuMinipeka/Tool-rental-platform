package com.toolrental.application.common.exception;

public final class ReferenceNotFoundApplicationException extends ApplicationException {
  public ReferenceNotFoundApplicationException(String reference) {
    super(reference + " not found");
  }
}
