package com.toolrental.infrastructure.payment.adapters.in.rest;

import com.toolrental.application.user.exception.AuthenticationFailedApplicationException;
import com.toolrental.infrastructure.payment.adapters.in.rest.dtos.SandboxWebhookRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class SandboxWebhookVerifier {
  private static final Duration MAX_AGE = Duration.ofMinutes(5);
  private final byte[] secret;
  private final Clock clock;

  public SandboxWebhookVerifier(
      @Value("${payments.sandbox.webhook-secret}") String secret, Clock clock) {
    if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32)
      throw new IllegalStateException("Sandbox webhook secret must contain at least 32 bytes");
    this.secret = secret.getBytes(StandardCharsets.UTF_8);
    this.clock = clock;
  }

  public void verify(String timestamp, String signature, SandboxWebhookRequest request) {
    try {
      var occurredAt = Instant.ofEpochSecond(Long.parseLong(timestamp));
      var age = Duration.between(occurredAt, Instant.now(clock)).abs();
      if (age.compareTo(MAX_AGE) > 0) throw new AuthenticationFailedApplicationException();
      var payload =
          timestamp
              + "."
              + request.eventId()
              + "."
              + request.externalReference()
              + "."
              + request.outcome();
      var mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret, "HmacSHA256"));
      var expected =
          java.util.HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
      if (signature == null
          || !MessageDigest.isEqual(
              expected.getBytes(StandardCharsets.US_ASCII),
              signature.getBytes(StandardCharsets.US_ASCII)))
        throw new AuthenticationFailedApplicationException();
    } catch (AuthenticationFailedApplicationException exception) {
      throw exception;
    } catch (RuntimeException exception) {
      throw new AuthenticationFailedApplicationException();
    } catch (java.security.GeneralSecurityException exception) {
      throw new IllegalStateException("Cannot verify sandbox webhook", exception);
    }
  }
}
