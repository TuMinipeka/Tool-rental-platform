package com.toolrental.infrastructure.payment.adapters.out.gateway;

import com.toolrental.application.common.exception.IdempotencyConflictApplicationException;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.payment.port.FictionalPaymentDecision;
import com.toolrental.domain.payment.port.PaymentGateway;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "payments.provider", havingValue = "fictional", matchIfMissing = true)
public final class FictionalPaymentGateway implements PaymentGateway, FictionalPaymentDecision {
  private final JdbcTemplate db;

  public FictionalPaymentGateway(JdbcTemplate db) {
    this.db = db;
  }

  public String processor() {
    return "FICTIONAL_GATEWAY";
  }

  public Checkout create(String reference, BigDecimal amount, Instant expiresAt) {
    var id = UUID.fromString(reference);
    if (db.queryForObject(
            "SELECT count(*) FROM payments WHERE payment_id=? AND processor='FICTIONAL_GATEWAY' AND amount=?",
            Integer.class,
            id,
            amount)
        != 1)
      throw new BusinessRuleException("Fictional checkout does not match a persisted payment");
    return new Checkout("fictional-" + id, "/checkout/" + id);
  }

  public Optional<Checkout> recoverCheckout(String reference) {
    var id = UUID.fromString(reference);
    return db.queryForObject(
                "SELECT count(*) FROM payments WHERE payment_id=? AND processor='FICTIONAL_GATEWAY'",
                Integer.class,
                id)
            == 1
        ? Optional.of(new Checkout("fictional-" + id, "/checkout/" + id))
        : Optional.empty();
  }

  public List<Result> search(String reference) {
    var id = UUID.fromString(reference);
    return outcome(id) == null ? List.of() : List.of(fetch(reference));
  }

  public Result fetch(String paymentId) {
    var id = UUID.fromString(paymentId);
    return db
        .query(
            "SELECT amount,fictional_outcome,fictional_refunded FROM payments WHERE payment_id=? AND processor='FICTIONAL_GATEWAY'",
            (r, n) ->
                new Result(
                    id.toString(),
                    id.toString(),
                    r.getBigDecimal("amount"),
                    "COP",
                    r.getBoolean("fictional_refunded")
                        ? "refunded"
                        : "APPROVED".equals(r.getString("fictional_outcome"))
                            ? "approved"
                            : "DECLINED".equals(r.getString("fictional_outcome"))
                                ? "rejected"
                                : "pending",
                    false,
                    "fictional-demo"),
            id)
        .stream()
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown fictional payment"));
  }

  public String outcome(UUID id) {
    return db
        .query(
            "SELECT COALESCE(fictional_outcome,'') FROM payments WHERE payment_id=? AND processor='FICTIONAL_GATEWAY'",
            (r, n) -> r.getString(1),
            id)
        .stream()
        .findFirst()
        .filter(value -> !value.isEmpty())
        .orElse(null);
  }

  public void decide(UUID id, String outcome) {
    int updated =
        db.update(
            "UPDATE payments SET fictional_outcome=? WHERE payment_id=? AND processor='FICTIONAL_GATEWAY' AND fictional_outcome IS NULL",
            outcome,
            id);
    if (updated == 0 && !outcome.equals(outcome(id)))
      throw new IdempotencyConflictApplicationException();
  }

  public void refund(String paymentId, BigDecimal amount, String key) {
    var id = UUID.fromString(paymentId);
    if (!key.equals("late-" + id)
        || db.update(
                "UPDATE payments SET fictional_refunded=true WHERE payment_id=? AND processor='FICTIONAL_GATEWAY' AND fictional_outcome='APPROVED' AND amount=?",
                id,
                amount)
            != 1) throw new BusinessRuleException("Invalid fictional refund");
  }
}
