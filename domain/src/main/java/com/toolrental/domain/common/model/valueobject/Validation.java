package com.toolrental.domain.common.model.valueobject;

import com.toolrental.domain.common.exception.BusinessRuleException;

public final class Validation {
  private Validation() {}

  public static String requiredText(String value, String field) {
    if (value == null || value.isBlank()) throw new BusinessRuleException(field + " is required");
    return value.strip();
  }
}
