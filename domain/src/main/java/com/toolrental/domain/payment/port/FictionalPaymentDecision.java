package com.toolrental.domain.payment.port;

import java.util.UUID;

/** Persists an explicitly academic decision; never contacts a financial institution. */
public interface FictionalPaymentDecision {
  String outcome(UUID paymentId);

  void decide(UUID paymentId, String outcome);
}
