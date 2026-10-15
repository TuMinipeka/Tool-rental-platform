package com.toolrental.infrastructure;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toolrental.application.reservation.command.RequestReservationCommand;
import com.toolrental.application.reservation.usecase.CancelReservationUseCase;
import com.toolrental.application.reservation.usecase.ConfirmReservationUseCase;
import com.toolrental.application.reservation.usecase.RequestReservationUseCase;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.customer.model.valueobject.CustomerId;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import jakarta.persistence.EntityManagerFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
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
@SpringBootTest
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
  UUID admin, supplier, customer, category, tool, unit;

  @BeforeEach
  void seed() {
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
        "insert into tools values (?,?,?,'Drill','Power drill',10,'COP','PUBLISHED')",
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
    if (actor != null) builder.principal(() -> actor.toString());
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
    return request
        .execute(
            new RequestReservationCommand(
                new CustomerId(customer),
                new ReservationPeriod(LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 21)),
                List.of(new EquipmentUnitId(unit))))
        .id();
  }

  @Test
  void bootsWithAllEighteenMappingsAndFreshMigrations() {
    assertThat(emf.getMetamodel().getEntities()).hasSize(18);
    assertThat(
            db.queryForObject(
                "select count(*) from flyway_schema_history where success and version is not null",
                Integer.class))
        .isEqualTo(3);
    assertThat(
            db.queryForObject(
                "select count(*) from information_schema.tables where table_schema=? and table_type='BASE TABLE' and table_name<>'flyway_schema_history'",
                Integer.class,
                schema))
        .isEqualTo(18);
  }

  @Test
  void httpRejectsAnonymousInvalidMissingAndUnauthorizedRequests() throws Exception {
    call(null, get("/api/users"), null, 401);
    call(null, post("/api/users"), Map.of(), 401);
    call(admin, post("/api/users"), Map.of(), 400);
    call(admin, get("/api/users/" + UUID.randomUUID()), null, 404);
    call(customer, get("/api/users/" + supplier), null, 403);
    call(customer, get("/api/payments"), null, 403);
    call(customer, get("/api/categories"), null, 200);
    call(customer, get("/api/tools"), null, 200);
    call(customer, get("/api/tools/" + tool), null, 200);
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
    call(supplier, post("/api/rentals"), Map.of("reservationId", rid), 201);
    call(customer, post("/api/reservations/" + rid + "/cancellation"), null, 409);
    call(supplier, patch("/api/rentals/" + rid + "/status"), Map.of("status", "COMPLETED"), 409);
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
    var payment =
        call(
            admin,
            post("/api/payments"),
            Map.of(
                "reservationId",
                rid,
                "processor",
                "MANUAL",
                "externalReference",
                "PAY-1",
                "amount",
                20),
            201);
    call(
        admin,
        patch("/api/payments/" + payment.get("paymentId").asText() + "/status"),
        Map.of("status", "CONFIRMED"),
        200);
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
    call(supplier, post("/api/rentals"), Map.of("reservationId", id.value()), 201);
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
                  } catch (BusinessRuleException ex) {
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
    var boundary =
        request
            .execute(
                new RequestReservationCommand(
                    new CustomerId(customer),
                    new ReservationPeriod(LocalDate.of(2026, 10, 21), LocalDate.of(2026, 10, 21)),
                    List.of(new EquipmentUnitId(unit))))
            .id();
    assertThatThrownBy(() -> confirm.execute(boundary, new SupplierId(supplier)))
        .isInstanceOf(BusinessRuleException.class);
    assertThat(
            db.queryForObject(
                "select reservation_status from reservations where reservation_id=?",
                String.class,
                boundary.value()))
        .isEqualTo("PENDING");
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
                              .principal(() -> supplier.toString())
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
