package com.toolrental.infrastructure;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toolrental.application.common.exception.AvailabilityConflictApplicationException;
import com.toolrental.application.reservation.command.RequestReservationCommand;
import com.toolrental.application.reservation.usecase.CancelReservationUseCase;
import com.toolrental.application.reservation.usecase.ConfirmReservationUseCase;
import com.toolrental.application.reservation.usecase.RequestReservationUseCase;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.customer.model.valueobject.CustomerId;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.aggregate.Reservation;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import com.toolrental.domain.reservation.port.repository.ReservationRepository;
import com.toolrental.domain.reservationitem.model.entity.ReservationLine;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import com.toolrental.domain.user.port.SecureTokenService;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;

/** Uses Docker by default; an explicit isolated PostgreSQL URL supports local validation. */
@SpringBootTest(
    properties = {
      "auth.password-recovery.enabled=true",
      "payments.internal-enabled=true",
      "payments.provider=mercadopago",
      "payments.mercadopago.collector-id=123",
      "payments.reconciliation-delay-ms=3600000",
      "payments.mercadopago.webhook-secret=official-test-webhook-secret-long-enough-2026"
    })
@AutoConfigureMockMvc
class PostgresIntegrationTest {
  static final String schema = "validation_" + UUID.randomUUID().toString().replace("-", "");
  static PostgreSQLContainer<?> container;

  @DynamicPropertySource
  static void database(DynamicPropertyRegistry registry) {
    String url = System.getProperty("postgres.test.url");
    String username = System.getProperty("postgres.test.username", "postgres");
    String password = System.getProperty("postgres.test.password", "");
    if (url == null) {
      container = new PostgreSQLContainer<>("postgres:17-alpine");
      container.start();
      url = container.getJdbcUrl();
      username = container.getUsername();
      password = container.getPassword();
    }
    final String finalUrl = url, finalUser = username, finalPassword = password;
    registry.add("spring.datasource.url", () -> finalUrl);
    registry.add("spring.datasource.username", () -> finalUser);
    registry.add("spring.datasource.password", () -> finalPassword);
    registry.add("spring.flyway.schemas", () -> schema);
    registry.add("spring.flyway.default-schema", () -> schema);
    registry.add("spring.jpa.properties.hibernate.default_schema", () -> schema);
    registry.add("spring.datasource.hikari.schema", () -> schema);
    registry.add("spring.datasource.hikari.minimum-idle", () -> 0);
  }

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired JdbcTemplate db;
  @Autowired EntityManagerFactory emf;
  @Autowired RequestReservationUseCase request;
  @Autowired ConfirmReservationUseCase confirm;
  @Autowired CancelReservationUseCase cancel;
  @Autowired SecureTokenService tokenService;
  @Autowired ReservationRepository reservationRepository;
  @Autowired Clock clock;

  @org.springframework.test.context.bean.override.mockito.MockitoBean
  com.toolrental.domain.payment.port.PaymentGateway gateway;

  @Autowired
  com.toolrental.application.payment.usecase.ReconcileHostedPaymentUseCase reconcileHosted;

  @Autowired com.toolrental.application.payment.usecase.CreateHostedCheckoutUseCase createHosted;
  UUID admin, supplier, customer, category, tool, unit;

  @BeforeEach
  void seed() {
    org.mockito.Mockito.reset(gateway);
    org.mockito.Mockito.when(gateway.processor()).thenReturn("MERCADO_PAGO");
    org.mockito.Mockito.when(gateway.search(org.mockito.ArgumentMatchers.anyString()))
        .thenReturn(List.of());
    org.mockito.Mockito.when(gateway.recoverCheckout(org.mockito.ArgumentMatchers.anyString()))
        .thenReturn(java.util.Optional.empty());
    org.mockito.Mockito.when(
            gateway.create(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
        .thenAnswer(
            invocation ->
                new com.toolrental.domain.payment.port.PaymentGateway.Checkout(
                    "preference-1",
                    "https://www.mercadopago.com.co/checkout/v1/redirect?pref_id=preference-1"));
    db.execute(
        "truncate invoice_items,invoices,payments,damage_reports,unit_returns,unit_deliveries,rentals,reservation_items,reservations,equipment_units,tool_images,tools,categories,customers,suppliers,user_roles,users cascade");
    admin = UUID.randomUUID();
    supplier = UUID.randomUUID();
    customer = UUID.randomUUID();
    category = UUID.randomUUID();
    tool = UUID.randomUUID();
    unit = UUID.randomUUID();
    for (var user : List.of(admin, supplier, customer))
      db.update(
          "insert into users values (?,?,?,'test-hash','ACTIVE',now())",
          user,
          user + "@example.com",
          "Test User");
    db.update("insert into user_roles select ?,role_id from roles where code='ADMIN'", admin);
    db.update("insert into user_roles select ?,role_id from roles where code='SUPPLIER'", supplier);
    db.update("insert into user_roles select ?,role_id from roles where code='CUSTOMER'", customer);
    db.update("insert into customers values (?,'PERSON','ID','C-1','Customer Original')", customer);
    db.update("insert into suppliers values (?,'ID','S-1','Supplier Original')", supplier);
    db.update("insert into categories values (?,'Drills','Full category description')", category);
    db.update(
        "insert into tools (tool_id,supplier_id,category_id,name,description,current_daily_rate,rate_currency,publication_status,service_city) values (?,?,?,'Drill','Power drill',10,'COP','PUBLISHED','Bucaramanga')",
        tool,
        supplier,
        category);
    db.update("insert into tool_images values (?,'https://example.com/drill.png')", tool);
    db.update(
        "insert into equipment_units values (?,?,'INV-1','SERIAL-123','GOOD','OPERATIONAL')",
        unit,
        tool);
  }

  JsonNode call(UUID actor, MockHttpServletRequestBuilder builder, Object body, int expected)
      throws Exception {
    if (actor != null)
      builder.with(jwt().jwt(token -> token.subject(actor.toString()).claim("purpose", "access")));
    if (body != null)
      builder.contentType("application/json").content(json.writeValueAsString(body));
    String result =
        mvc.perform(builder)
            .andExpect(status().is(expected))
            .andReturn()
            .getResponse()
            .getContentAsString();
    return result.isBlank() ? json.nullNode() : json.readTree(result);
  }

  ReservationId pending() {
    var start = LocalDate.now(ZoneId.of("America/Bogota")).plusDays(1);
    var reservation =
        Reservation.request(
            new CustomerId(customer),
            new ReservationPeriod(start, start.plusDays(1)),
            List.of(
                new ReservationLine(
                    new EquipmentUnitId(unit),
                    new SupplierId(supplier),
                    new Money(new BigDecimal("10"), Currency.getInstance("COP")))),
            clock);
    reservationRepository.addRequest(reservation);
    return reservation.id();
  }

  com.toolrental.application.reservation.dto.ReservationResponse held() {
    var start = LocalDate.now(ZoneId.of("America/Bogota")).plusDays(1);
    return request.execute(
        new RequestReservationCommand(
            new CustomerId(customer),
            new ReservationPeriod(start, start.plusDays(1)),
            List.of(new EquipmentUnitId(unit))));
  }

  @Test
  void bootsWithAllTwentyFiveMappingsAndFreshMigrations() {
    assertThat(emf.getMetamodel().getEntities()).hasSize(25);
    assertThat(
            db.queryForObject(
                "select count(*) from flyway_schema_history where success and version is not null",
                Integer.class))
        .isEqualTo(38);
    assertThat(
            db.queryForObject(
                "select count(*) from information_schema.tables where table_schema=? and table_type='BASE TABLE' and table_name<>'flyway_schema_history'",
                Integer.class,
                schema))
        .isEqualTo(25);
  }

  @Test
  void httpRejectsAnonymousInvalidMissingAndUnauthorizedRequests() throws Exception {
    call(null, get("/api/users"), null, 401);
    call(null, post("/api/users"), Map.of(), 401);
    call(admin, post("/api/users"), Map.of(), 400);
    call(admin, get("/api/users/" + UUID.randomUUID()), null, 404);
    call(customer, get("/api/users/" + supplier), null, 403);
    call(customer, get("/api/payments"), null, 200);
    call(customer, get("/api/categories"), null, 200);
    call(customer, get("/api/tools"), null, 200);
    call(customer, get("/api/tools/" + tool), null, 200);
  }

  @Test
  void authenticationRecoveryPublicCatalogAndOpenApiUseThePublishedContracts() throws Exception {
    String email = "new.customer@example.com";
    String initialPassword = "InitialPass123!";
    String changedPassword = "ChangedPass123!";
    var registration =
        call(
            null,
            post("/api/auth/register"),
            Map.of(
                "email",
                email,
                "displayName",
                "New Customer",
                "password",
                initialPassword,
                "personType",
                "PERSON",
                "documentType",
                "ID",
                "documentNumber",
                "NEW-CUSTOMER-1",
                "billingName",
                "New Customer"),
            201);
    assertThat(registration.get("roles").toString()).contains("CUSTOMER");

    var login =
        call(
            null,
            post("/api/auth/login"),
            Map.of("email", email, "password", initialPassword),
            200);
    String accessToken = login.get("accessToken").asText();
    call(
        null, get("/api/reservations").header("Authorization", "Bearer " + accessToken), null, 200);

    var recovery = call(null, post("/api/v1/auth/password-recovery"), Map.of("email", email), 202);
    assertThat(recovery.has("recoveryToken")).isFalse();
    String resetToken = "integration-reset-token";
    db.update(
        "insert into password_reset_tokens (password_reset_token_id,user_id,token_hash,created_at,expires_at) values (?,?,?,now(),now()+interval '15 minutes')",
        UUID.randomUUID(),
        UUID.fromString(registration.get("userId").asText()),
        tokenService.hash(resetToken));
    call(
        null,
        post("/api/v1/auth/password-reset"),
        Map.of("token", resetToken, "newPassword", changedPassword),
        204);
    call(
        null,
        get("/api/v1/reservations").header("Authorization", "Bearer " + accessToken),
        null,
        401);
    call(
        null,
        post("/api/v1/auth/password-reset"),
        Map.of("token", resetToken, "newPassword", changedPassword),
        400);
    call(null, post("/api/auth/login"), Map.of("email", email, "password", initialPassword), 401);
    call(
        null, post("/api/v1/auth/login"), Map.of("email", email, "password", changedPassword), 200);

    assertThat(call(null, get("/api/public/catalog"), null, 200).toString()).contains("Drill");
    assertThat(call(null, get("/api/v1/public/catalog"), null, 200).toString()).contains("Drill");
    assertThat(
            call(null, get("/api/v1/public/catalog").param("serviceCity", "Bucaramanga"), null, 200)
                .get(0)
                .get("serviceCity")
                .asText())
        .isEqualTo("Bucaramanga");
    assertThat(
            call(
                null,
                get("/api/v1/public/catalog").param("serviceCity", "Floridablanca"),
                null,
                200))
        .isEmpty();
    assertThat(call(null, get("/v3/api-docs"), null, 200).get("openapi").asText()).startsWith("3.");
  }

  @Test
  void supplierOnboardingAndProfileDeactivationEnforceJwtRolesAndSessionRevocation()
      throws Exception {
    String supplierEmail = "candidate.supplier@example.com";
    String supplierPassword = "SupplierPass123!";
    var supplierRequest =
        Map.of(
            "email",
            supplierEmail,
            "displayName",
            "Candidate Supplier",
            "password",
            supplierPassword,
            "documentType",
            "NIT",
            "documentNumber",
            "SUPPLIER-9001",
            "businessName",
            "Candidate Tools");
    var registration = call(null, post("/api/auth/supplier-registration"), supplierRequest, 202);
    var candidateId = UUID.fromString(registration.get("userId").asText());
    assertThat(registration.get("accountStatus").asText()).isEqualTo("SUSPENDED");
    call(
        null,
        post("/api/auth/login"),
        Map.of("email", supplierEmail, "password", supplierPassword),
        401);
    call(customer, patch("/api/users/" + candidateId + "/status"), Map.of("status", "ACTIVE"), 403);
    call(admin, patch("/api/users/" + candidateId + "/status"), Map.of("status", "ACTIVE"), 200);

    var supplierLogin =
        call(
            null,
            post("/api/auth/login"),
            Map.of("email", supplierEmail, "password", supplierPassword),
            200);
    assertThat(supplierLogin.get("roles").toString()).contains("SUPPLIER");
    var supplierToken = supplierLogin.get("accessToken").asText();
    var today = LocalDate.now(ZoneId.of("America/Bogota"));
    call(
        null,
        get("/api/reports/availability?from=" + today + "&to=" + today)
            .header("Authorization", "Bearer " + supplierToken),
        null,
        200);
    call(null, get("/api/users").header("Authorization", "Bearer " + supplierToken), null, 403);

    call(
        null,
        post("/api/auth/supplier-registration"),
        Map.of(
            "email",
            "duplicate.document@example.com",
            "displayName",
            "Duplicate Supplier",
            "password",
            supplierPassword,
            "documentType",
            "NIT",
            "documentNumber",
            "SUPPLIER-9001",
            "businessName",
            "Duplicate Tools"),
        409);
    call(
        null,
        post("/api/auth/supplier-registration"),
        Map.of(
            "email",
            "invalid.supplier@example.com",
            "displayName",
            "Invalid Supplier",
            "password",
            "short",
            "documentType",
            "NIT",
            "documentNumber",
            "SUPPLIER-9002",
            "businessName",
            "Invalid Tools"),
        400);

    call(
        null,
        delete("/api/suppliers/" + candidateId).header("Authorization", "Bearer " + supplierToken),
        null,
        204);
    assertThat(
            db.queryForObject(
                "select account_status from users where user_id=?", String.class, candidateId))
        .isEqualTo("DISABLED");
    assertThat(
            db.queryForObject(
                "select count(*) from suppliers where user_id=?", Integer.class, candidateId))
        .isEqualTo(1);
    assertThat(
            db.queryForObject(
                "select count(*) from user_sessions where user_id=? and session_status='REVOKED'",
                Integer.class,
                candidateId))
        .isEqualTo(1);
    call(null, get("/api/tools").header("Authorization", "Bearer " + supplierToken), null, 401);

    String customerEmail = "deactivated.customer@example.com";
    var customerRegistration =
        call(
            null,
            post("/api/auth/register"),
            Map.of(
                "email",
                customerEmail,
                "displayName",
                "Deactivated Customer",
                "password",
                "CustomerPass123!",
                "personType",
                "PERSON",
                "documentType",
                "ID",
                "documentNumber",
                "CUSTOMER-9001",
                "billingName",
                "Deactivated Customer"),
            201);
    var customerId = UUID.fromString(customerRegistration.get("userId").asText());
    var customerToken = customerRegistration.get("accessToken").asText();
    call(
        null,
        get("/api/reports/income?from=" + today + "&to=" + today)
            .header("Authorization", "Bearer " + customerToken),
        null,
        403);
    call(
        null,
        delete("/api/customers/" + customerId).header("Authorization", "Bearer " + customerToken),
        null,
        204);
    assertThat(
            db.queryForObject(
                "select account_status from users where user_id=?", String.class, customerId))
        .isEqualTo("DISABLED");
    assertThat(
            db.queryForObject(
                "select count(*) from customers where user_id=?", Integer.class, customerId))
        .isEqualTo(1);
    call(
        null,
        get("/api/reservations").header("Authorization", "Bearer " + customerToken),
        null,
        401);
  }

  com.toolrental.domain.payment.port.PaymentGateway.Result providerResult(UUID id, String status) {
    return new com.toolrental.domain.payment.port.PaymentGateway.Result(
        "9001", id.toString(), new BigDecimal("20"), "COP", status, false, "123");
  }

  @Test
  void hostedCheckoutRequiresActorAndVerifiesSignedProviderApprovalExactlyOnce() throws Exception {
    var held = held();
    call(
        null,
        post("/api/v1/payments/checkouts").header("Idempotency-Key", "official-1"),
        Map.of("reservationId", held.id().value()),
        401);
    var checkout =
        call(
            customer,
            post("/api/v1/payments/checkouts").header("Idempotency-Key", "official-1"),
            Map.of("reservationId", held.id().value()),
            200);
    var id = UUID.fromString(checkout.path("paymentId").asText());
    assertThat(checkout.path("checkoutUrl").asText()).startsWith("https://www.mercadopago.com.co/");
    call(
        customer,
        post("/api/v1/payments/checkouts").header("Idempotency-Key", "official-1"),
        Map.of("reservationId", held.id().value()),
        200);
    call(supplier, get("/api/v1/payments/checkouts/" + id), null, 200);
    org.mockito.Mockito.verify(gateway, org.mockito.Mockito.times(1))
        .create(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any());
    org.mockito.Mockito.when(gateway.fetch("9001")).thenReturn(providerResult(id, "approved"));
    call(
        null,
        post("/api/v1/payments/mercadopago/webhooks?data.id=9001")
            .header("x-request-id", "request-1")
            .header("x-signature", "ts=1,v1=bad"),
        null,
        401);
    var ts = Long.toString(Instant.now().getEpochSecond());
    var mac = javax.crypto.Mac.getInstance("HmacSHA256");
    mac.init(
        new javax.crypto.spec.SecretKeySpec(
            "official-test-webhook-secret-long-enough-2026"
                .getBytes(java.nio.charset.StandardCharsets.UTF_8),
            "HmacSHA256"));
    var sig =
        java.util.HexFormat.of()
            .formatHex(
                mac.doFinal(
                    ("id:9001;request-id:request-1;ts:" + ts + ";")
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    for (int n = 0; n < 2; n++)
      call(
          null,
          post("/api/v1/payments/mercadopago/webhooks?data.id=9001")
              .header("x-request-id", "request-1")
              .header("x-signature", "ts=" + ts + ",v1=" + sig),
          null,
          200);
    assertThat(
            db.queryForObject(
                "select reservation_status from reservations where reservation_id=?",
                String.class,
                held.id().value()))
        .isEqualTo("CONFIRMED");
    assertThat(db.queryForObject("select count(*) from invoices", Integer.class)).isEqualTo(1);
    assertThat(
            call(customer, get("/api/v1/payments/checkouts/" + id), null, 200)
                .path("paymentStatus")
                .asText())
        .isEqualTo("DONE");
    org.mockito.Mockito.when(gateway.fetch("9001")).thenReturn(providerResult(id, "rejected"));
    reconcileHosted.webhook("9001");
    assertThat(
            db.queryForObject(
                "select payment_status from payments where payment_id=?", String.class, id))
        .isEqualTo("CONFIRMED");
  }

  @Test
  void hostedCheckoutConcurrentRequestsReuseOnePersistedAttempt() throws Exception {
    var held = held();
    try (var pool = Executors.newFixedThreadPool(2)) {
      var gate = new CyclicBarrier(2);
      var jobs =
          new ArrayList<Future<com.toolrental.application.payment.dto.HostedCheckoutResponse>>();
      for (int i = 0; i < 2; i++) {
        final var key = "parallel-" + i;
        jobs.add(
            pool.submit(
                () -> {
                  gate.await();
                  return createHosted.execute(customer, held.id(), key);
                }));
      }
      var first = jobs.get(0).get(20, TimeUnit.SECONDS);
      var second = jobs.get(1).get(20, TimeUnit.SECONDS);
      assertThat(first.paymentId()).isEqualTo(second.paymentId());
    }
    assertThat(
            db.queryForObject(
                "select count(*) from payments where processor='MERCADO_PAGO'", Integer.class))
        .isEqualTo(1);
    org.mockito.Mockito.verify(gateway, org.mockito.Mockito.times(1))
        .create(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any());
  }

  @Test
  void lateHostedApprovalRefundsWithoutRestoringExpiredInventory() {
    var held = held();
    var checkout = createHosted.execute(customer, held.id(), "late-1");
    db.update(
        "update reservations set expires_at=now()-interval '1 minute' where reservation_id=?",
        held.id().value());
    org.mockito.Mockito.when(gateway.fetch("9001"))
        .thenReturn(providerResult(checkout.paymentId(), "approved"));
    reconcileHosted.webhook("9001");
    reconcileHosted.webhook("9001");
    assertThat(
            db.queryForObject(
                "select payment_status from payments where payment_id=?",
                String.class,
                checkout.paymentId()))
        .isEqualTo("REFUNDED");
    assertThat(
            db.queryForObject(
                "select reservation_status from reservations where reservation_id=?",
                String.class,
                held.id().value()))
        .isEqualTo("EXPIRED");
    assertThat(db.queryForObject("select count(*) from invoices", Integer.class)).isZero();
    org.mockito.Mockito.verify(gateway, org.mockito.Mockito.times(1))
        .refund("9001", new BigDecimal("20.0000"), "late-" + checkout.paymentId());
  }

  @Test
  void failedLateRefundRetainsItsLedgerAndRetriesWithTheSameKey() {
    var held = held();
    var checkout = createHosted.execute(customer, held.id(), "refund-retry-1");
    db.update(
        "update reservations set expires_at=now()-interval '1 minute' where reservation_id=?",
        held.id().value());
    org.mockito.Mockito.when(gateway.fetch("9001"))
        .thenReturn(providerResult(checkout.paymentId(), "approved"));
    org.mockito.Mockito.doThrow(new IllegalStateException("Temporary refund outage"))
        .doNothing()
        .when(gateway)
        .refund(
            org.mockito.ArgumentMatchers.eq("9001"),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.eq("late-" + checkout.paymentId()));
    assertThatThrownBy(() -> reconcileHosted.webhook("9001"))
        .isInstanceOf(IllegalStateException.class);
    assertThat(
            db.queryForObject(
                "select refund_status from refunds where payment_id=?",
                String.class,
                checkout.paymentId()))
        .isEqualTo("PENDING");
    reconcileHosted.webhook("9001");
    assertThat(
            db.queryForObject(
                "select refund_status from refunds where payment_id=?",
                String.class,
                checkout.paymentId()))
        .isEqualTo("CONFIRMED");
    assertThat(
            db.queryForObject(
                "select count(*) from refunds where payment_id=?",
                Integer.class,
                checkout.paymentId()))
        .isEqualTo(1);
  }

  @Test
  void officialApprovalRollsBackPaymentAndReservationWhenInvoiceCannotBeSaved() {
    var held = held();
    var checkout = createHosted.execute(customer, held.id(), "invoice-rollback-1");
    org.mockito.Mockito.when(gateway.fetch("9001"))
        .thenReturn(providerResult(checkout.paymentId(), "approved"));
    db.execute(
        "CREATE FUNCTION reject_test_invoice() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'Invoice unavailable'; END $$");
    db.execute(
        "CREATE TRIGGER reject_test_invoice BEFORE INSERT ON invoices FOR EACH ROW EXECUTE FUNCTION reject_test_invoice()");
    try {
      assertThatThrownBy(() -> reconcileHosted.webhook("9001"))
          .isInstanceOf(RuntimeException.class);
      assertThat(
              db.queryForObject(
                  "select payment_status from payments where payment_id=?",
                  String.class,
                  checkout.paymentId()))
          .isEqualTo("PENDING");
      assertThat(
              db.queryForObject(
                  "select reservation_status from reservations where reservation_id=?",
                  String.class,
                  held.id().value()))
          .isEqualTo("PENDING_PAYMENT");
      assertThat(db.queryForObject("select count(*) from invoices", Integer.class)).isZero();
    } finally {
      db.execute("DROP TRIGGER reject_test_invoice ON invoices");
      db.execute("DROP FUNCTION reject_test_invoice()");
    }
    reconcileHosted.webhook("9001");
    assertThat(db.queryForObject("select count(*) from invoices", Integer.class)).isEqualTo(1);
  }

  @Test
  void mismatchedOrLiveProviderPaymentNeverConfirmsReservation() {
    var held = held();
    var checkout = createHosted.execute(customer, held.id(), "mismatch-1");
    var invalid =
        List.of(
            new com.toolrental.domain.payment.port.PaymentGateway.Result(
                "9001",
                checkout.paymentId().toString(),
                BigDecimal.ONE,
                "COP",
                "approved",
                false,
                "123"),
            new com.toolrental.domain.payment.port.PaymentGateway.Result(
                "9001",
                checkout.paymentId().toString(),
                new BigDecimal("20"),
                "USD",
                "approved",
                false,
                "123"),
            new com.toolrental.domain.payment.port.PaymentGateway.Result(
                "9001",
                checkout.paymentId().toString(),
                new BigDecimal("20"),
                "COP",
                "approved",
                true,
                "123"),
            new com.toolrental.domain.payment.port.PaymentGateway.Result(
                "9001",
                checkout.paymentId().toString(),
                new BigDecimal("20"),
                "COP",
                "approved",
                false,
                "456"));
    for (var result : invalid) {
      org.mockito.Mockito.when(gateway.fetch("9001")).thenReturn(result);
      assertThatThrownBy(() -> reconcileHosted.webhook("9001"))
          .isInstanceOf(com.toolrental.domain.common.exception.BusinessRuleException.class);
    }
    assertThat(
            db.queryForObject(
                "select payment_status from payments where payment_id=?",
                String.class,
                checkout.paymentId()))
        .isEqualTo("PENDING");
    assertThat(db.queryForObject("select count(*) from invoices", Integer.class)).isZero();
  }

  @Test
  void interruptedHostedCreationIsReconciledWithoutCreatingAnotherPreference() {
    var held = held();
    org.mockito.Mockito.when(
            gateway.create(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
        .thenThrow(new IllegalStateException("Provider timeout"));
    assertThatThrownBy(() -> createHosted.execute(customer, held.id(), "interrupted-1"))
        .isInstanceOf(IllegalStateException.class);
    var id =
        db.queryForObject(
            "select payment_id from payments where processor='MERCADO_PAGO'", UUID.class);
    org.mockito.Mockito.when(gateway.recoverCheckout(id.toString()))
        .thenReturn(
            java.util.Optional.of(
                new com.toolrental.domain.payment.port.PaymentGateway.Checkout(
                    "recovered-1",
                    "https://www.mercadopago.com.co/checkout/v1/redirect?pref_id=recovered-1")));
    var recovered = createHosted.execute(customer, held.id(), "interrupted-1");
    assertThat(recovered.paymentId()).isEqualTo(id);
    assertThat(recovered.paymentStatus()).isEqualTo("READY");
    org.mockito.Mockito.verify(gateway, org.mockito.Mockito.times(1))
        .create(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any());
  }

  @Test
  void hostedRejectionReleasesHoldAndSubsequentApprovalIsRefunded() {
    var held = held();
    var checkout = createHosted.execute(customer, held.id(), "declined-1");
    org.mockito.Mockito.when(gateway.fetch("9001"))
        .thenReturn(providerResult(checkout.paymentId(), "rejected"));
    reconcileHosted.webhook("9001");
    assertThat(
            db.queryForObject(
                "select reservation_status from reservations where reservation_id=?",
                String.class,
                held.id().value()))
        .isEqualTo("PAYMENT_FAILED");
    assertThat(held().status().name()).isEqualTo("PENDING_PAYMENT");
    org.mockito.Mockito.when(gateway.fetch("9001"))
        .thenReturn(providerResult(checkout.paymentId(), "approved"));
    reconcileHosted.webhook("9001");
    assertThat(
            db.queryForObject(
                "select payment_status from payments where payment_id=?",
                String.class,
                checkout.paymentId()))
        .isEqualTo("REFUNDED");
    assertThat(db.queryForObject("select count(*) from invoices", Integer.class)).isZero();
  }

  @Test
  void signedSandboxWebhookConfirmsHoldAndIssuesDownloadableAcademicInvoice() throws Exception {
    var reservation = held();
    assertThat(reservation.status().name()).isEqualTo("PENDING_PAYMENT");
    assertThat(reservation.expiresAt()).isAfter(Instant.now());
    assertThatThrownBy(this::held).isInstanceOf(AvailabilityConflictApplicationException.class);

    var payment =
        call(
            customer,
            post("/api/payments/internal/checkouts").header("Idempotency-Key", "full-checkout-1"),
            Map.of(
                "reservationId",
                reservation.id().value(),
                "paymentMethod",
                "CREDIT_CARD",
                "cardNetwork",
                "VISA",
                "cardLastFour",
                "4242",
                "cardExpiryMonth",
                12,
                "cardExpiryYear",
                2030),
            201);
    assertThat(payment.get("amount").decimalValue()).isEqualByComparingTo("20");
    assertThat(payment.get("paymentStatus").asText()).isEqualTo("PENDING");

    var eventId = "event-approved-1";
    var reference = payment.get("externalReference").asText();
    var outcome = "APPROVED";
    var timestamp = Long.toString(Instant.now().getEpochSecond());
    var webhookBody =
        Map.of("eventId", eventId, "externalReference", reference, "outcome", outcome);
    call(
        null,
        post("/api/payments/sandbox/webhooks")
            .header("X-Sandbox-Timestamp", timestamp)
            .header("X-Sandbox-Signature", "invalid"),
        webhookBody,
        401);
    var signature = sandboxSignature(timestamp, eventId, reference, outcome);
    var confirmedPayment =
        call(
            null,
            post("/api/payments/sandbox/webhooks")
                .header("X-Sandbox-Timestamp", timestamp)
                .header("X-Sandbox-Signature", signature),
            webhookBody,
            200);
    assertThat(confirmedPayment.get("paymentStatus").asText()).isEqualTo("CONFIRMED");
    assertThat(
            db.queryForObject(
                "select reservation_status from reservations where reservation_id=?",
                String.class,
                reservation.id().value()))
        .isEqualTo("CONFIRMED");
    assertThat(db.queryForObject("select count(*) from invoices", Integer.class)).isEqualTo(1);

    var invoices = call(customer, get("/api/invoices"), null, 200);
    var invoiceId = invoices.get(0).get("invoiceId").asText();
    mvc.perform(
            get("/api/invoices/" + invoiceId + "/document")
                .with(jwt().jwt(token -> token.subject(customer.toString()))))
        .andExpect(status().isOk())
        .andExpect(
            header()
                .string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
        .andExpect(
            content()
                .string(
                    org.hamcrest.Matchers.containsString("Simulaci&oacute;n acad&eacute;mica")));
  }

  @Test
  void operationalReportsExposeRealDataWithinRoleScope() throws Exception {
    var reservationId = pending();
    confirm.execute(reservationId, new SupplierId(supplier));
    db.update(
        """
        insert into payments (
          payment_id, reservation_id, processor, external_reference, amount, payment_status,
          recorded_at, confirmed_at, payment_method, payment_stage, idempotency_key
        ) values (?, ?, 'ACADEMIC_TEST', ?, 20, 'CONFIRMED', now(), now(),
                  'ADMINISTRATIVE', 'ADMINISTRATIVE', ?)
        """,
        UUID.randomUUID(),
        reservationId.value(),
        "report-payment-" + reservationId.value(),
        "report-idempotency-" + reservationId.value());

    var today = LocalDate.now(ZoneId.of("America/Bogota"));
    var period = "?from=" + today.minusDays(1) + "&to=" + today.plusDays(3);
    var income = call(supplier, get("/api/reports/income" + period), null, 200);
    assertThat(income.get(0).get("grossIncome").decimalValue()).isEqualByComparingTo("20");
    assertThat(income.get(0).get("netIncome").decimalValue()).isEqualByComparingTo("20");

    var mostRented = call(supplier, get("/api/reports/most-rented-tools" + period), null, 200);
    assertThat(mostRented.get(0).get("toolId").asText()).isEqualTo(tool.toString());
    assertThat(mostRented.get(0).get("reservationCount").asLong()).isEqualTo(1);
    assertThat(mostRented.get(0).get("bookedRevenue").decimalValue()).isEqualByComparingTo("20");

    var customers = call(supplier, get("/api/reports/frequent-customers" + period), null, 200);
    assertThat(customers.get(0).get("customerId").asText()).isEqualTo(customer.toString());

    var availability = call(supplier, get("/api/reports/availability" + period), null, 200);
    assertThat(availability.get(0).get("reservedUnits").asLong()).isEqualTo(1);
    assertThat(availability.get(0).get("availableUnits").asLong()).isZero();

    var profitability = call(supplier, get("/api/reports/profitability" + period), null, 200);
    assertThat(profitability.get(0).get("bookedRevenue").decimalValue()).isEqualByComparingTo("20");
    call(admin, get("/api/reports/income" + period + "&supplierId=" + supplier), null, 200);
    call(customer, get("/api/reports/income" + period), null, 403);
  }

  String sandboxSignature(String timestamp, String eventId, String reference, String outcome)
      throws Exception {
    var payload = timestamp + "." + eventId + "." + reference + "." + outcome;
    var mac = javax.crypto.Mac.getInstance("HmacSHA256");
    mac.init(
        new javax.crypto.spec.SecretKeySpec(
            "local-sandbox-webhook-secret-change-me-2026"
                .getBytes(java.nio.charset.StandardCharsets.UTF_8),
            "HmacSHA256"));
    return java.util.HexFormat.of()
        .formatHex(mac.doFinal(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
  }

  @Test
  void mapsPreviouslyMissingAttributesAndIndependentImages() throws Exception {
    assertThat(
            call(admin, get("/api/categories/" + category), null, 200).get("description").asText())
        .isEqualTo("Full category description");
    var equipment = call(supplier, get("/api/equipment-units/" + unit), null, 200);
    assertThat(equipment.get("serialNumber").asText()).isEqualTo("SERIAL-123");
    assertThat(equipment.get("currentCondition").asText()).isEqualTo("GOOD");
    call(
        supplier,
        post("/api/tool-images"),
        Map.of("toolId", tool, "imageUrl", "https://example.com/second.png"),
        201);
    assertThat(call(supplier, get("/api/tools/" + tool), null, 200).get("imageUrls")).hasSize(2);
    call(
        supplier,
        delete("/api/tool-images/entry")
            .param("toolId", tool.toString())
            .param("imageUrl", "https://example.com/second.png"),
        null,
        204);
    var reservation = pending();
    db.update(
        "update reservations set expires_at='2026-10-19T12:00:00Z' where reservation_id=?",
        reservation.value());
    confirm.execute(reservation, new SupplierId(supplier));
    assertThat(
            db.queryForObject(
                    "select expires_at from reservations where reservation_id=?",
                    java.sql.Timestamp.class,
                    reservation.value())
                .toInstant())
        .isEqualTo(Instant.parse("2026-10-19T12:00:00Z"));
  }

  @Test
  void identityUsesHashedPasswordsAndPreservesSharedAndCompositeKeys() throws Exception {
    var account =
        call(
            admin,
            post("/api/users"),
            Map.of(
                "email",
                "new@example.com",
                "displayName",
                "New customer",
                "password",
                "A-long-test-password"),
            201);
    UUID user = UUID.fromString(account.get("userId").asText());
    assertThat(account.has("passwordHash")).isFalse();
    assertThat(
            db.queryForObject(
                "select password_hash from users where user_id=?", String.class, user))
        .startsWith("$2")
        .doesNotContain("A-long-test-password");
    var role = db.queryForObject("select role_id from roles where code='CUSTOMER'", Short.class);
    call(admin, post("/api/user-roles"), Map.of("userId", user, "roleId", role), 201);
    call(
        user,
        post("/api/customers"),
        Map.of(
            "userId",
            user,
            "personType",
            "PERSON",
            "documentType",
            "ID",
            "documentNumber",
            "NEW-1",
            "billingName",
            "New Name"),
        201);
    call(
        user,
        put("/api/customers/" + user),
        Map.of(
            "userId",
            user,
            "personType",
            "PERSON",
            "documentType",
            "ID",
            "documentNumber",
            "NEW-1",
            "billingName",
            "Changed Name"),
        200);
    assertThat(
            db.queryForObject(
                "select billing_name from customers where user_id=?", String.class, user))
        .isEqualTo("Changed Name");
    call(admin, patch("/api/users/" + user + "/status"), Map.of("status", "DISABLED"), 200);
    call(admin, patch("/api/users/" + user + "/status"), Map.of("status", "ACTIVE"), 409);
  }

  @Test
  void rentalDeliveryReturnDamagePaymentAndInvoiceWorkflowsAreAtomic() throws Exception {
    var reservation = pending();
    confirm.execute(reservation, new SupplierId(supplier));
    UUID rid = reservation.value();
    var firstPaymentRequest =
        Map.of(
            "reservationId",
            rid,
            "paymentStage",
            "FIRST_HALF",
            "paymentMethod",
            "CREDIT_CARD",
            "idempotencyKey",
            "reservation-first-half",
            "cardNetwork",
            "VISA",
            "cardLastFour",
            "4242",
            "cardExpiryMonth",
            12,
            "cardExpiryYear",
            2030,
            "simulationOutcome",
            "APPROVE");
    var firstPayment = call(customer, post("/api/payments/simulations"), firstPaymentRequest, 201);
    assertThat(firstPayment.get("amount").decimalValue()).isEqualByComparingTo("10.0000");
    assertThat(firstPayment.get("cardLastFour").asText()).isEqualTo("4242");
    assertThat(
            call(customer, post("/api/payments/simulations"), firstPaymentRequest, 201)
                .get("paymentId")
                .asText())
        .isEqualTo(firstPayment.get("paymentId").asText());
    call(supplier, post("/api/rentals"), Map.of("reservationId", rid), 409);
    call(
        supplier,
        post("/api/unit-deliveries"),
        Map.of(
            "reservationId",
            rid,
            "equipmentUnitId",
            unit,
            "scheduledAt",
            Instant.now().minusSeconds(3600).toString(),
            "outboundCondition",
            "GOOD",
            "addressLine",
            "Street 1",
            "city",
            "Bogota",
            "region",
            "DC",
            "country",
            "CO"),
        201);
    call(
        supplier,
        post("/api/unit-returns"),
        Map.of(
            "reservationId",
            rid,
            "equipmentUnitId",
            unit,
            "inboundCondition",
            "SCRATCHED",
            "notes",
            "Test return"),
        409);
    call(
        supplier,
        patch("/api/unit-deliveries/entry/completion")
            .param("reservationId", rid.toString())
            .param("equipmentUnitId", unit.toString()),
        Map.of("condition", "GOOD"),
        200);
    call(supplier, post("/api/rentals"), Map.of("reservationId", rid), 201);
    call(
        customer,
        post("/api/reservations/" + rid + "/cancellation")
            .header("Idempotency-Key", "after-rental"),
        null,
        409);
    call(supplier, patch("/api/rentals/" + rid + "/status"), Map.of("status", "COMPLETED"), 409);
    call(
        supplier,
        post("/api/unit-returns"),
        Map.of(
            "reservationId",
            rid,
            "equipmentUnitId",
            unit,
            "inboundCondition",
            "SCRATCHED",
            "notes",
            "Test return"),
        201);
    call(supplier, patch("/api/rentals/" + rid + "/status"), Map.of("status", "COMPLETED"), 200);
    var damage =
        call(
            supplier,
            post("/api/damage-reports"),
            Map.of(
                "reservationId",
                rid,
                "equipmentUnitId",
                unit,
                "description",
                "Scratch on returned casing",
                "assessedCost",
                0),
            201);
    var damageId = damage.get("damageReportId").asText();
    call(
        supplier, patch("/api/damage-reports/" + damageId + "/assessment"), Map.of("cost", 5), 200);
    call(
        supplier,
        patch("/api/damage-reports/" + damageId + "/status"),
        Map.of("status", "CLOSED"),
        200);
    var finalPayment =
        call(
            customer,
            post("/api/payments/simulations"),
            Map.of(
                "reservationId",
                rid,
                "paymentStage",
                "FINAL_HALF",
                "paymentMethod",
                "PAYPAL",
                "idempotencyKey",
                "reservation-final-half",
                "simulationOutcome",
                "APPROVE"),
            201);
    assertThat(finalPayment.get("amount").decimalValue()).isEqualByComparingTo("10.0000");
    var invoice =
        call(
            supplier,
            post("/api/invoices"),
            Map.of(
                "reservationId",
                rid,
                "number",
                "INV-1",
                "lines",
                List.of(
                    Map.of(
                        "description",
                        "Two inclusive rental days",
                        "quantity",
                        2,
                        "unitPrice",
                        10))),
            201);
    var iid = invoice.get("invoiceId").asText();
    assertThat(invoice.get("issuedCustomerName").asText()).isEqualTo("Customer Original");
    db.update("update customers set billing_name='Changed later' where user_id=?", customer);
    assertThat(
            call(supplier, get("/api/invoices/" + iid), null, 200)
                .get("issuedCustomerName")
                .asText())
        .isEqualTo("Customer Original");
    call(
        customer,
        get("/api/invoice-items/entry").param("invoiceId", iid).param("lineNumber", "1"),
        null,
        200);
    call(
        supplier,
        put("/api/invoice-items/entry").param("invoiceId", iid).param("lineNumber", "1"),
        Map.of(),
        405);
    call(supplier, patch("/api/invoices/" + iid + "/status"), Map.of("status", "PAID"), 200);
    call(supplier, patch("/api/invoices/" + iid + "/status"), Map.of("status", "VOIDED"), 409);
  }

  @Test
  void failedInvoiceLineRollsBackHeaderAndOtherLines() throws Exception {
    var id = pending();
    confirm.execute(id, new SupplierId(supplier));
    var rid = id.value();
    call(
        supplier,
        post("/api/unit-deliveries"),
        Map.of(
            "reservationId",
            rid,
            "equipmentUnitId",
            unit,
            "scheduledAt",
            Instant.now().minusSeconds(3600).toString(),
            "outboundCondition",
            "GOOD",
            "addressLine",
            "Street 1",
            "city",
            "Bogota",
            "region",
            "DC",
            "country",
            "CO"),
        201);
    call(
        supplier,
        patch("/api/unit-deliveries/entry/completion")
            .param("reservationId", rid.toString())
            .param("equipmentUnitId", unit.toString()),
        Map.of("condition", "GOOD"),
        200);
    call(supplier, post("/api/rentals"), Map.of("reservationId", rid), 201);
    call(
        supplier,
        post("/api/invoices"),
        Map.of(
            "reservationId",
            id.value(),
            "number",
            "ROLLBACK-1",
            "lines",
            List.of(
                Map.of("description", "valid", "quantity", 1, "unitPrice", 10),
                Map.of(
                    "description",
                    "invalid",
                    "quantity",
                    new java.math.BigDecimal("1.00001"),
                    "unitPrice",
                    10))),
        409);
    assertThat(db.queryForObject("select count(*) from invoices", Integer.class)).isZero();
    assertThat(db.queryForObject("select count(*) from invoice_items", Integer.class)).isZero();
  }

  @Test
  void concurrentConfirmationsHaveOneWinnerAndCancellationReleasesUnit() throws Exception {
    var a = pending();
    var b = pending();
    var gate = new CyclicBarrier(2);
    try (var pool = Executors.newFixedThreadPool(2)) {
      List<Future<Boolean>> results = new ArrayList<>();
      for (var id : List.of(a, b))
        results.add(
            pool.submit(
                () -> {
                  gate.await(10, TimeUnit.SECONDS);
                  try {
                    confirm.execute(id, new SupplierId(supplier));
                    return true;
                  } catch (AvailabilityConflictApplicationException ex) {
                    return false;
                  }
                }));
      assertThat(
              results.get(0).get(20, TimeUnit.SECONDS) ^ results.get(1).get(20, TimeUnit.SECONDS))
          .isTrue();
    }
    UUID winner =
        db.queryForObject(
            "select reservation_id from reservations where reservation_status='CONFIRMED'",
            UUID.class);
    assertThat(
            db.queryForObject(
                "select count(*) from reservations where reservation_status='PENDING'",
                Integer.class))
        .isEqualTo(1);
    db.update("update tools set current_daily_rate=99 where tool_id=?", tool);
    assertThat(
            db.queryForObject(
                "select agreed_daily_rate from reservation_items where reservation_id=?",
                java.math.BigDecimal.class,
                winner))
        .isEqualByComparingTo("10");
    cancel.execute(new ReservationId(winner), new CustomerId(customer));
    var loser = a.value().equals(winner) ? b : a;
    confirm.execute(loser, new SupplierId(supplier));
    assertThat(
            db.queryForObject(
                "select agreed_daily_rate from reservation_items where reservation_id=?",
                java.math.BigDecimal.class,
                loser.value()))
        .isEqualByComparingTo("99");
  }

  @Test
  void inclusiveBoundaryConflictAndSupplierInventoryUniquenessAreEnforced() throws Exception {
    var first = pending();
    confirm.execute(first, new SupplierId(supplier));
    var inclusiveReturn =
        db.queryForObject(
            "select return_date from reservations where reservation_id=?",
            LocalDate.class,
            first.value());
    assertThatThrownBy(
            () ->
                request.execute(
                    new RequestReservationCommand(
                        new CustomerId(customer),
                        new ReservationPeriod(inclusiveReturn, inclusiveReturn),
                        List.of(new EquipmentUnitId(unit)))))
        .isInstanceOf(AvailabilityConflictApplicationException.class);
    assertThat(
            db.queryForObject(
                "select count(*) from reservations where customer_id=?", Integer.class, customer))
        .isEqualTo(1);
    call(
        supplier,
        post("/api/equipment-units"),
        Map.of(
            "toolId",
            tool,
            "inventoryCode",
            "inv-1",
            "serialNumber",
            "other",
            "currentCondition",
            "GOOD",
            "status",
            "OPERATIONAL"),
        409);
  }

  @Test
  void catalogAndSupplierAdministrationPreserveDetails() throws Exception {
    var c =
        call(
            admin,
            post("/api/categories"),
            Map.of("name", "Saws", "description", "Cutting tools"),
            201);
    UUID cid = UUID.fromString(c.get("id").get("value").asText());
    var offer =
        call(
            supplier,
            post("/api/tools"),
            Map.of(
                "categoryId",
                cid,
                "name",
                "Saw",
                "description",
                "Circular saw",
                "serviceCity",
                "Floridablanca",
                "dailyRate",
                15,
                "currency",
                "COP",
                "imageUrls",
                List.of("https://example.com/saw.png"),
                "published",
                true),
            201);
    UUID tid = UUID.fromString(offer.get("id").get("value").asText());
    call(
        supplier,
        put("/api/tools/" + tid),
        Map.of(
            "name",
            "Updated saw",
            "description",
            "Updated description",
            "serviceCity",
            "Piedecuesta",
            "dailyRate",
            18,
            "currency",
            "COP",
            "published",
            true),
        200);
    var physical =
        call(
            supplier,
            post("/api/equipment-units"),
            Map.of(
                "toolId",
                tid,
                "inventoryCode",
                "SAW-1",
                "serialNumber",
                "SERIAL-SAW",
                "currentCondition",
                "NEW",
                "status",
                "OPERATIONAL"),
            201);
    UUID uid = UUID.fromString(physical.get("id").get("value").asText());
    call(
        supplier,
        put("/api/equipment-units/" + uid),
        Map.of("serialNumber", "SERIAL-SAW", "currentCondition", "GOOD", "status", "RETIRED"),
        200);
    call(
        supplier,
        put("/api/equipment-units/" + uid),
        Map.of("serialNumber", "SERIAL-SAW", "currentCondition", "GOOD", "status", "OPERATIONAL"),
        409);
    call(admin, delete("/api/categories/" + cid), null, 409);
    call(
        supplier,
        put("/api/suppliers/" + supplier),
        Map.of(
            "userId",
            supplier,
            "documentType",
            "ID",
            "documentNumber",
            "S-1",
            "businessName",
            "Updated Supplier"),
        200);
    var role =
        call(
            admin,
            post("/api/roles"),
            Map.of("code", "AUDITOR", "description", "Read audit information"),
            201);
    call(
        admin,
        put("/api/roles/" + role.get("roleId").asInt()),
        Map.of("code", "AUDITOR", "description", "Updated role description"),
        200);
  }

  @Test
  void concurrentInventoryRegistrationHasOneWinner() throws Exception {
    var gate = new CyclicBarrier(2);
    try (var pool = Executors.newFixedThreadPool(2)) {
      var tasks = new ArrayList<Future<Integer>>();
      for (var code : List.of("NEW-CODE", "new-code"))
        tasks.add(
            pool.submit(
                () -> {
                  gate.await(10, TimeUnit.SECONDS);
                  return mvc.perform(
                          post("/api/equipment-units")
                              .with(
                                  jwt()
                                      .jwt(
                                          token ->
                                              token
                                                  .subject(supplier.toString())
                                                  .claim("purpose", "access")))
                              .contentType("application/json")
                              .content(
                                  json.writeValueAsString(
                                      Map.of(
                                          "toolId",
                                          tool,
                                          "inventoryCode",
                                          code,
                                          "serialNumber",
                                          "TEST",
                                          "currentCondition",
                                          "GOOD",
                                          "status",
                                          "OPERATIONAL"))))
                      .andReturn()
                      .getResponse()
                      .getStatus();
                }));
      assertThat(
              List.of(
                  tasks.get(0).get(20, TimeUnit.SECONDS), tasks.get(1).get(20, TimeUnit.SECONDS)))
          .containsExactlyInAnyOrder(201, 409);
    }
    assertThat(
            db.queryForObject(
                "select count(*) from equipment_units where lower(inventory_code)='new-code'",
                Integer.class))
        .isEqualTo(1);
  }

  @Test
  void exposesLiveDatabaseStatusAndPageWithoutOpeningBusinessRoutes() throws Exception {
    var status = call(null, get("/status/database"), null, 200);
    assertThat(status.get("connected").asBoolean()).isTrue();
    assertThat(status.has("password")).isFalse();
    mvc.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("index.html"));
    mvc.perform(get("/index.html"))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("status-title")));
    mvc.perform(get("/connection-status.js")).andExpect(status().isOk());
    call(null, get("/api/users"), null, 401);
  }
}
