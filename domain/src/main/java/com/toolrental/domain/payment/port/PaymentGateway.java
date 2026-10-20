package com.toolrental.domain.payment.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface PaymentGateway {
  default String processor() {
    return "MERCADO_PAGO";
  }

  record Checkout(String preferenceId, String url) {}

  record Result(
      String id,
      String reference,
      BigDecimal amount,
      String currency,
      String status,
      boolean live,
      String collectorId) {}

  Checkout create(String reference, BigDecimal amount, Instant expiresAt);

  java.util.Optional<Checkout> recoverCheckout(String reference);

  List<Result> search(String reference);

  Result fetch(String paymentId);

  void refund(String paymentId, BigDecimal amount, String idempotencyKey);
}
