package com.toolrental.infrastructure.toolimage.exception;

import com.toolrental.infrastructure.common.exception.InfrastructureException;

public final class ToolImageStorageInfrastructureException extends InfrastructureException {
  public ToolImageStorageInfrastructureException(Throwable cause) {
    super("Tool image storage is unavailable", cause);
  }
}
