package com.toolrental.infrastructure.common.web;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/public/features", "/api/public/features"})
public final class PublicFeaturesController {
  private final boolean hosted;
  private final String provider;

  public PublicFeaturesController(
      @Value("${payments.mercadopago.access-token:}") String token,
      @Value("${payments.mercadopago.collector-id:}") String collector,
      @Value("${payments.mercadopago.webhook-secret:}") String secret,
      @Value("${payments.mercadopago.public-url:}") String url,
      @Value("${payments.provider:fictional}") String provider) {
    this.provider = provider.equals("fictional") ? "FICTIONAL_GATEWAY" : "MERCADO_PAGO";
    this.hosted =
        provider.equals("fictional")
            || (!token.isBlank()
                && !collector.isBlank()
                && secret.length() >= 32
                && url.startsWith("https://"));
  }

  @GetMapping
  public Map<String, Object> features() {
    return Map.of(
        "hostedCheckout",
        hosted,
        "paymentProvider",
        provider,
        "currency",
        "COP");
  }
}
