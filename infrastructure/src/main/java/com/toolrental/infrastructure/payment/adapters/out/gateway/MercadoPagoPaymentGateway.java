package com.toolrental.infrastructure.payment.adapters.out.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toolrental.domain.payment.port.PaymentGateway;
import com.toolrental.infrastructure.payment.exception.PaymentGatewayInfrastructureException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
    name = "payments.provider",
    havingValue = "mercadopago")
public final class MercadoPagoPaymentGateway implements PaymentGateway {
  private final HttpClient http;
  private final ObjectMapper json;
  private final URI base;
  private final String token;
  private final String collector;
  private final String publicUrl;
  private volatile boolean verifiedTestAccount;

  @Autowired
  public MercadoPagoPaymentGateway(
      ObjectMapper json,
      @Value("${payments.mercadopago.access-token:}") String token,
      @Value("${payments.mercadopago.collector-id:}") String collector,
      @Value("${payments.mercadopago.public-url:}") String publicUrl) {
    this(
        HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build(),
        json,
        URI.create("https://api.mercadopago.com"),
        token,
        collector,
        publicUrl);
  }

  MercadoPagoPaymentGateway(
      HttpClient http,
      ObjectMapper json,
      URI base,
      String token,
      String collector,
      String publicUrl) {
    this.http = http;
    this.json = json;
    this.base = base;
    this.token = token;
    this.collector = collector;
    this.publicUrl = publicUrl;
  }

  private void requireTestAccount() {
    if (verifiedTestAccount) return;
    if (token.isBlank() || collector.isBlank() || !publicUrl.startsWith("https://"))
      throw new PaymentGatewayInfrastructureException();
    var account = call("GET", "/users/me", null, null);
    boolean test = false;
    for (var tag : account.path("tags")) if (tag.asText().equals("test_user")) test = true;
    if (!test || !account.path("id").asText().equals(collector))
      throw new PaymentGatewayInfrastructureException();
    verifiedTestAccount = true;
  }

  public Checkout create(String reference, BigDecimal amount, Instant expiresAt) {
    requireTestAccount();
    var back = publicUrl + "/payment-return?paymentId=" + reference;
    var body =
        Map.of(
            "external_reference",
            reference,
            "items",
            List.of(
                Map.of(
                    "id",
                    reference,
                    "title",
                    "Tool rental academic reservation",
                    "quantity",
                    1,
                    "currency_id",
                    "COP",
                    "unit_price",
                    amount)),
            "back_urls",
            Map.of("success", back, "failure", back, "pending", back),
            "notification_url",
            publicUrl + "/api/v1/payments/mercadopago/webhooks",
            "expires",
            true,
            "expiration_date_to",
            expiresAt.toString(),
            "payment_methods",
            Map.of(
                "excluded_payment_types",
                List.of(
                    Map.of("id", "ticket"), Map.of("id", "bank_transfer"), Map.of("id", "atm"))),
            "binary_mode",
            true);
    var result = call("POST", "/checkout/preferences", body, reference);
    var url = result.path("init_point").asText();
    var parsed = URI.create(url);
    if (!"https".equals(parsed.getScheme())
        || !Set.of("www.mercadopago.com.co", "www.mercadopago.com", "mercadopago.com.co")
            .contains(parsed.getHost())
        || result.path("id").asText().isBlank()) throw new PaymentGatewayInfrastructureException();
    return new Checkout(result.path("id").asText(), url);
  }

  public java.util.Optional<Checkout> recoverCheckout(String reference) {
    requireTestAccount();
    var response =
        call(
            "GET",
            "/checkout/preferences/search?external_reference="
                + URLEncoder.encode(reference, StandardCharsets.UTF_8),
            null,
            null);
    var elements = response.path("elements");
    if (!elements.isArray() || elements.size() > 1)
      throw new PaymentGatewayInfrastructureException();
    if (elements.isEmpty()) return java.util.Optional.empty();
    var id = elements.get(0).path("id").asText();
    if (!id.matches("[a-zA-Z0-9-]{1,200}")) throw new PaymentGatewayInfrastructureException();
    var stored = call("GET", "/checkout/preferences/" + id, null, null);
    if (!stored.path("external_reference").asText().equals(reference))
      throw new PaymentGatewayInfrastructureException();
    var url = stored.path("init_point").asText();
    var parsed = URI.create(url);
    if (!"https".equals(parsed.getScheme())
        || !Set.of("www.mercadopago.com.co", "www.mercadopago.com", "mercadopago.com.co")
            .contains(parsed.getHost())) throw new PaymentGatewayInfrastructureException();
    return java.util.Optional.of(new Checkout(id, url));
  }

  public List<Result> search(String reference) {
    requireTestAccount();
    var response =
        call(
            "GET",
            "/v1/payments/search?external_reference="
                + URLEncoder.encode(reference, StandardCharsets.UTF_8)
                + "&sort=date_created&criteria=desc&limit=100",
            null,
            null);
    var list = new ArrayList<Result>();
    for (var row : response.path("results")) list.add(map(row));
    if (response.path("paging").path("total").asInt() > list.size())
      throw new PaymentGatewayInfrastructureException();
    return List.copyOf(list);
  }

  public Result fetch(String id) {
    requireTestAccount();
    if (!id.matches("[0-9]{1,30}"))
      throw new IllegalArgumentException("Invalid provider payment identifier");
    return map(call("GET", "/v1/payments/" + id, null, null));
  }

  public void refund(String id, BigDecimal amount, String key) {
    requireTestAccount();
    var verified = fetch(id);
    if (verified.live()
        || !collector.equals(verified.collectorId())
        || !verified.currency().equals("COP")
        || amount.compareTo(verified.amount()) != 0)
      throw new PaymentGatewayInfrastructureException();
    if (verified.status().equals("refunded")) return;
    var result = call("POST", "/v1/payments/" + id + "/refunds", Map.of("amount", amount), key);
    if (!result.path("status").asText().equals("approved"))
      throw new PaymentGatewayInfrastructureException();
  }

  private Result map(JsonNode row) {
    if (!row.hasNonNull("id")
        || !row.hasNonNull("transaction_amount")
        || !row.has("live_mode")
        || !row.hasNonNull("collector_id")) throw new PaymentGatewayInfrastructureException();
    return new Result(
        row.path("id").asText(),
        row.path("external_reference").asText(),
        row.path("transaction_amount").decimalValue(),
        row.path("currency_id").asText(),
        row.path("status").asText(),
        row.path("live_mode").asBoolean(true),
        row.path("collector_id").asText());
  }

  private JsonNode call(String method, String path, Object body, String key) {
    try {
      var request =
          HttpRequest.newBuilder(base.resolve(path))
              .timeout(Duration.ofSeconds(10))
              .header("Authorization", "Bearer " + token)
              .header("Content-Type", "application/json");
      if (key != null) request.header("X-Idempotency-Key", key);
      request.method(
          method,
          body == null
              ? HttpRequest.BodyPublishers.noBody()
              : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
      var response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300)
        throw new PaymentGatewayInfrastructureException();
      return json.reader()
          .with(com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
          .readTree(response.body());
    } catch (InterruptedException error) {
      Thread.currentThread().interrupt();
      throw new PaymentGatewayInfrastructureException();
    } catch (java.io.IOException | IllegalArgumentException error) {
      throw new PaymentGatewayInfrastructureException();
    }
  }
}
