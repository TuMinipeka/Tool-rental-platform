package com.toolrental.infrastructure.payment.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotNull;

public record FictionalPaymentDecisionRequest(@NotNull Outcome outcome) {
  public enum Outcome {
    APPROVED,
    DECLINED
  }
}
