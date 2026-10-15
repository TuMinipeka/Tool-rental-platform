package com.toolrental.application.catalog.exception;

import com.toolrental.application.common.exception.ApplicationException;
import com.toolrental.domain.catalog.model.valueobject.ToolId;
import java.util.Objects;

public final class ToolNotFoundApplicationException extends ApplicationException {
  public ToolNotFoundApplicationException(ToolId id) {
    super("Tool not found: " + Objects.requireNonNull(id).value());
  }

  public ToolNotFoundApplicationException(ToolId id, Throwable cause) {
    super("Tool not found: " + Objects.requireNonNull(id).value(), cause);
  }
}
