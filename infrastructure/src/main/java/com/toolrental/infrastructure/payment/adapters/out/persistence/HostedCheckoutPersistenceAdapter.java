package com.toolrental.infrastructure.payment.adapters.out.persistence;

import com.toolrental.domain.payment.model.valueobject.HostedCheckout;
import com.toolrental.domain.payment.port.repository.HostedCheckoutRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Updates checkout orchestration columns on the existing payment row; no second JPA owner. */
@Repository
public class HostedCheckoutPersistenceAdapter implements HostedCheckoutRepository {
  private final JdbcTemplate db;
  private final String processor;

  public HostedCheckoutPersistenceAdapter(
      JdbcTemplate db, com.toolrental.domain.payment.port.PaymentGateway gateway) {
    this.db = db;
    this.processor = gateway.processor();
  }

  public void initialize(UUID id, Instant expiresAt) {
    db.update(
        "UPDATE payments SET checkout_expires_at=?, checkout_phase='CREATING' WHERE payment_id=?",
        Timestamp.from(expiresAt),
        id);
  }

  public Optional<HostedCheckout> find(UUID id) {
    return db
        .query(
            "SELECT * FROM payments WHERE payment_id=? AND processor IN ('MERCADO_PAGO','FICTIONAL_GATEWAY')",
            (r, n) ->
                new HostedCheckout(
                    r.getObject("payment_id", UUID.class),
                    r.getString("processor"),
                    r.getObject("reservation_id", UUID.class),
                    r.getBigDecimal("amount"),
                    r.getTimestamp("checkout_expires_at").toInstant(),
                    r.getString("checkout_preference_id"),
                    r.getString("checkout_url"),
                    r.getString("provider_payment_id"),
                    r.getString("checkout_phase"),
                    r.getInt("checkout_attempts"),
                    r.getTimestamp("checkout_lease_until") == null
                        ? null
                        : r.getTimestamp("checkout_lease_until").toInstant()),
            id)
        .stream()
        .findFirst();
  }

  public boolean claim(UUID id, Instant now, Instant leaseUntil) {
    return db.update(
            "UPDATE payments SET checkout_lease_until=?, checkout_attempts=checkout_attempts+1 "
                + "WHERE payment_id=? AND (checkout_lease_until IS NULL OR checkout_lease_until<=?)",
            Timestamp.from(leaseUntil),
            id,
            Timestamp.from(now))
        == 1;
  }

  public void release(UUID id) {
    db.update("UPDATE payments SET checkout_lease_until=NULL WHERE payment_id=?", id);
  }

  public void ready(UUID id, String preferenceId, String url) {
    db.update(
        "UPDATE payments SET checkout_preference_id=?, checkout_url=?, checkout_phase='READY', "
            + "checkout_retry_at=NULL WHERE payment_id=?",
        preferenceId,
        url,
        id);
  }

  public void phase(UUID id, String phase, String providerId, Instant retryAt) {
    db.update(
        "UPDATE payments SET checkout_phase=?, provider_payment_id=COALESCE(?,provider_payment_id), "
            + "checkout_retry_at=? WHERE payment_id=?",
        phase,
        providerId,
        retryAt == null ? null : Timestamp.from(retryAt),
        id);
  }

  public List<UUID> due(Instant now, int limit) {
    return db.query(
        "SELECT payment_id FROM payments WHERE processor=? AND checkout_phase IN "
            + "('CREATING','READY','REVIEW','REFUND_PENDING') AND checkout_attempts<100 "
            + "AND (checkout_retry_at IS NULL OR checkout_retry_at<=?) ORDER BY recorded_at LIMIT ?",
        (r, n) -> r.getObject(1, UUID.class),
        processor,
        Timestamp.from(now),
        limit);
  }
}
