package com.toolrental.infrastructure.payment.adapters.in.rest;

import com.toolrental.application.user.exception.AuthenticationFailedApplicationException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class MercadoPagoWebhookVerifier {
  private final String secret;
  private final Clock clock;

  public MercadoPagoWebhookVerifier(
      @Value("${payments.mercadopago.webhook-secret:}") String secret, Clock clock) {
    this.secret = secret;
    this.clock = clock;
  }

  public void verify(String id, String requestId, String signature) {
    try {
      if (secret.length() < 32
          || requestId == null
          || requestId.isBlank()
          || requestId.length() > 200
          || id == null
          || !id.matches("[0-9]{1,30}")
          || signature == null
          || signature.length() > 500) throw new AuthenticationFailedApplicationException();
      var parts = new HashMap<String, String>();
      for (var entry : signature.split(",")) {
        var pair = entry.trim().split("=", 2);
        if (pair.length != 2 || parts.put(pair[0], pair[1]) != null)
          throw new AuthenticationFailedApplicationException();
      }
      var ts = parts.get("ts");
      long value = Long.parseLong(ts);
      var time = value > 100000000000L ? Instant.ofEpochMilli(value) : Instant.ofEpochSecond(value);
      if (Duration.between(time, Instant.now(clock)).abs().compareTo(Duration.ofMinutes(5)) > 0)
        throw new AuthenticationFailedApplicationException();
      var manifest = "id:" + id + ";request-id:" + requestId + ";ts:" + ts + ";";
      var mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      if (!MessageDigest.isEqual(
          mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8)),
          HexFormat.of().parseHex(parts.get("v1"))))
        throw new AuthenticationFailedApplicationException();
    } catch (AuthenticationFailedApplicationException error) {
      throw error;
    } catch (Exception error) {
      throw new AuthenticationFailedApplicationException();
    }
  }
}
