package com.toolrental.infrastructure.payment.adapters.in.rest.controllers;

import com.toolrental.application.payment.usecase.ReconcileHostedPaymentUseCase;
import com.toolrental.infrastructure.payment.adapters.in.rest.MercadoPagoWebhookVerifier;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@ConditionalOnProperty(name = "payments.provider", havingValue = "mercadopago")
@RequestMapping({"/api/v1/payments/mercadopago", "/api/payments/mercadopago"})
@SecurityRequirements
public final class MercadoPagoWebhookController {
  private final MercadoPagoWebhookVerifier verifier;
  private final ReconcileHostedPaymentUseCase reconcile;

  public MercadoPagoWebhookController(
      MercadoPagoWebhookVerifier verifier, ReconcileHostedPaymentUseCase reconcile) {
    this.verifier = verifier;
    this.reconcile = reconcile;
  }

  @PostMapping("/webhooks")
  @ResponseStatus(HttpStatus.OK)
  public void receive(
      @RequestParam("data.id") String paymentId,
      @RequestHeader("x-request-id") String requestId,
      @RequestHeader("x-signature") String signature) {
    verifier.verify(paymentId, requestId, signature);
    reconcile.webhook(paymentId);
  }
}
