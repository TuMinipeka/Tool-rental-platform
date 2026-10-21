package com.toolrental.infrastructure.payment.adapters.out.gateway;

import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.toolrental.infrastructure.payment.exception.PaymentGatewayInfrastructureException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MercadoPagoPaymentGatewayTest {
  HttpServer server;
  MercadoPagoPaymentGateway gateway;
  ObjectMapper json = new ObjectMapper();
  List<String> calls = new ArrayList<>();
  AtomicReference<String> preferenceBody = new AtomicReference<>();
  String account = "{\"id\":123,\"tags\":[\"test_user\"]}";
  String payment =
      "{\"id\":9001,\"external_reference\":\"reference-1\",\"transaction_amount\":20,\"currency_id\":\"COP\",\"status\":\"approved\",\"live_mode\":false,\"collector_id\":123}";
  boolean fail;

  @BeforeEach
  void start() throws Exception {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/",
        exchange -> {
          var path = exchange.getRequestURI().getPath();
          calls.add(path);
          String body = "{}";
          if (path.equals("/users/me")) body = account;
          else if (path.equals("/checkout/preferences")
              && exchange.getRequestMethod().equals("POST")) {
            preferenceBody.set(
                new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            body =
                "{\"id\":\"pref-1\",\"init_point\":\"https://www.mercadopago.com.co/checkout/v1/redirect?pref_id=pref-1\"}";
          } else if (path.equals("/v1/payments/search"))
            body =
                json.writeValueAsString(
                    java.util.Map.of(
                        "results",
                        List.of(json.readTree(payment)),
                        "paging",
                        java.util.Map.of("total", 1)));
          else if (path.equals("/v1/payments/9001")) body = payment;
          else if (path.equals("/v1/payments/9001/refunds")) {
            assertThat(exchange.getRequestHeaders().getFirst("X-Idempotency-Key"))
                .isEqualTo("late-reference-1");
            body = "{\"status\":\"approved\"}";
          } else if (path.equals("/checkout/preferences/search"))
            body = "{\"elements\":[{\"id\":\"pref-1\"}]}";
          else if (path.equals("/checkout/preferences/pref-1"))
            body =
                "{\"external_reference\":\"reference-1\",\"init_point\":\"https://www.mercadopago.com.co/checkout/v1/redirect?pref_id=pref-1\"}";
          var bytes = body.getBytes(StandardCharsets.UTF_8);
          exchange.sendResponseHeaders(fail ? 503 : 200, bytes.length);
          exchange.getResponseBody().write(bytes);
          exchange.close();
        });
    server.start();
    gateway =
        new MercadoPagoPaymentGateway(
            HttpClient.newHttpClient(),
            json,
            URI.create("http://127.0.0.1:" + server.getAddress().getPort()),
            "private-test-token",
            "123",
            "https://demo.example.test");
  }

  @AfterEach
  void stop() {
    server.stop(0);
  }

  @Test
  void hostedPreferenceUsesServerAmountCopExpiryAndVerifiedTestSeller() throws Exception {
    var expires = Instant.now().plusSeconds(600);
    var checkout = gateway.create("reference-1", new BigDecimal("20"), expires);
    assertThat(checkout.preferenceId()).isEqualTo("pref-1");
    var body = json.readTree(preferenceBody.get());
    assertThat(body.path("items").get(0).path("currency_id").asText()).isEqualTo("COP");
    assertThat(body.path("items").get(0).path("unit_price").decimalValue())
        .isEqualByComparingTo("20");
    assertThat(body.path("expiration_date_to").asText()).isEqualTo(expires.toString());
    assertThat(body.path("notification_url").asText())
        .endsWith("/api/v1/payments/mercadopago/webhooks");
    assertThat(preferenceBody.get())
        .doesNotContain("card_number", "security_code", "private-test-token");
    assertThat(calls.getFirst()).isEqualTo("/users/me");
  }

  @Test
  void realSellerAccountCannotCreateAcademicCheckout() {
    account = "{\"id\":123,\"tags\":[]}";
    assertThatThrownBy(() -> gateway.create("reference-1", BigDecimal.TEN, Instant.now()))
        .isInstanceOf(PaymentGatewayInfrastructureException.class);
    assertThat(calls).containsExactly("/users/me");
  }

  @Test
  void ambiguousPreferenceIsRecoveredAndPaymentIsReadFromProvider() {
    assertThat(gateway.recoverCheckout("reference-1")).isPresent();
    assertThat(gateway.search("reference-1")).hasSize(1);
    var payment = gateway.fetch("9001");
    assertThat(payment.live()).isFalse();
    assertThat(payment.collectorId()).isEqualTo("123");
    gateway.refund("9001", new BigDecimal("20"), "late-reference-1");
    assertThat(calls).contains("/v1/payments/9001/refunds");
  }

  @Test
  void providerFailureExposesNoResponseBodyOrCredentials() {
    fail = true;
    assertThatThrownBy(() -> gateway.search("reference-1"))
        .isInstanceOf(PaymentGatewayInfrastructureException.class)
        .hasMessageNotContaining("private-test-token");
  }

  @Test
  void livePaymentCannotBeRefundedBySandboxAdapter() {
    payment = payment.replace("false", "true");
    assertThatThrownBy(() -> gateway.refund("9001", new BigDecimal("20"), "late-reference-1"))
        .isInstanceOf(PaymentGatewayInfrastructureException.class);
    assertThat(calls).doesNotContain("/v1/payments/9001/refunds");
  }
}
