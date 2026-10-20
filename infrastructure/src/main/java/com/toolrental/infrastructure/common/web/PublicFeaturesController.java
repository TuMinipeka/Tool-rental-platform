package com.toolrental.infrastructure.common.web;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/public/features", "/api/public/features"})
public final class PublicFeaturesController {
  private final boolean recovery;
  private final boolean hosted;

  public PublicFeaturesController(
      @Value("${auth.password-recovery.enabled:false}") boolean recovery,
      @Value("${payments.mercadopago.access-token:}") String token,
      @Value("${payments.mercadopago.collector-id:}") String collector,
      @Value("${payments.mercadopago.webhook-secret:}") String secret,
      @Value("${payments.mercadopago.public-url:}") String url) {
    this.recovery = recovery;
    this.hosted =
        !token.isBlank()
            && !collector.isBlank()
            && secret.length() >= 32
            && url.startsWith("https://");
  }

  @GetMapping
  public Map<String, Object> features() {
    return Map.of(
        "passwordRecovery",
        recovery,
        "hostedCheckout",
        hosted,
        "paymentProvider",
        "MERCADO_PAGO",
        "currency",
        "COP");
  }
}
