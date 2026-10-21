package com.toolrental.domain.payment.port.repository;

import com.toolrental.domain.payment.model.valueobject.HostedCheckout;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HostedCheckoutRepository {
  void initialize(UUID paymentId, Instant expiresAt);

  Optional<HostedCheckout> find(UUID paymentId);

  boolean claim(UUID paymentId, Instant now, Instant leaseUntil);

  void release(UUID paymentId);

  void ready(UUID paymentId, String preferenceId, String url);

  void phase(UUID paymentId, String phase, String providerPaymentId, Instant retryAt);

  List<UUID> due(Instant now, int limit);
}
