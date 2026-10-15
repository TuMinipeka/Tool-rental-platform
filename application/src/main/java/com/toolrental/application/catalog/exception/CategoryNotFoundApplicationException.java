package com.toolrental.application.catalog.exception;

import com.toolrental.application.common.exception.ApplicationException;
import com.toolrental.domain.catalog.model.valueobject.CategoryId;
import java.util.Objects;

public final class CategoryNotFoundApplicationException extends ApplicationException {
  public CategoryNotFoundApplicationException(CategoryId id) {
    super("Category not found: " + Objects.requireNonNull(id).value());
  }

  public CategoryNotFoundApplicationException(CategoryId id, Throwable cause) {
    super("Category not found: " + Objects.requireNonNull(id).value(), cause);
  }
}
