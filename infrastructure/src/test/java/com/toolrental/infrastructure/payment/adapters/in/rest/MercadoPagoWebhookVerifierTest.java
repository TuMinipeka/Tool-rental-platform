package com.toolrental.infrastructure.payment.adapters.in.rest;

import static org.assertj.core.api.Assertions.*;

import com.toolrental.application.user.exception.AuthenticationFailedApplicationException;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

class MercadoPagoWebhookVerifierTest {
  final Instant now = Instant.parse("2026-10-20T12:00:00Z");
  final String secret = "official-webhook-test-secret-at-least-32-bytes";
  final MercadoPagoWebhookVerifier verifier =
      new MercadoPagoWebhookVerifier(secret, Clock.fixed(now, ZoneOffset.UTC));

  String signature(Instant time) throws Exception {
    var ts = Long.toString(time.toEpochMilli());
    var mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    return "ts="
        + ts
        + ",v1="
        + HexFormat.of()
            .formatHex(
                mac.doFinal(
                    ("id:9001;request-id:req-1;ts:" + ts + ";").getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  void acceptsOfficialManifestAndRejectsChangedIdentifierOrRequestId() throws Exception {
    var signature = signature(now);
    verifier.verify("9001", "req-1", signature);
    assertThatThrownBy(() -> verifier.verify("9002", "req-1", signature))
        .isInstanceOf(AuthenticationFailedApplicationException.class);
    assertThatThrownBy(() -> verifier.verify("9001", "req-2", signature))
        .isInstanceOf(AuthenticationFailedApplicationException.class);
  }

  @Test
  void rejectsExpiredAndMalformedSignatures() throws Exception {
    var signature = signature(now.minusSeconds(301));
    assertThatThrownBy(() -> verifier.verify("9001", "req-1", signature))
        .isInstanceOf(AuthenticationFailedApplicationException.class);
    for (var invalid : java.util.List.of("ts=1,v1=xyz", "ts=1,ts=2,v1=ab", "v1=ab"))
      assertThatThrownBy(() -> verifier.verify("9001", "req-1", invalid))
          .isInstanceOf(AuthenticationFailedApplicationException.class);
  }
}
