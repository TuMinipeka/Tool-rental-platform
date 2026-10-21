package com.toolrental.application.toolimage.exception;

import com.toolrental.application.common.exception.ApplicationException;

public final class ToolImageNotFoundApplicationException extends ApplicationException {
  public ToolImageNotFoundApplicationException() {
    super("ToolImage not found");
  }
}
