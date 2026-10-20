package com.toolrental.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.toolrental.application.common.exception.AvailabilityConflictApplicationException;
import com.toolrental.application.reservation.usecase.ConfirmReservationUseCase;
import com.toolrental.domain.category.port.repository.CategoryRepository;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("local")
class PostgreSqlPersistenceIntegrationTest {
  private static final List<String> BUSINESS_TABLES =
      List.of(
          "users",
          "user_roles",
          "customers",
          "suppliers",
          "categories",
          "tools",
          "tool_images",
          "equipment_units",
          "reservations",
          "reservation_items",
          "rentals",
          "unit_deliveries",
          "unit_returns",
          "damage_reports",
          "payments",
          "invoices",
          "invoice_items",
          "reservation_terminations",
          "reservation_extensions",
          "refunds");

  static PostgreSQLContainer<?> postgres;
  static final String schema = "demo_validation_" + UUID.randomUUID().toString().replace("-", "");

  @DynamicPropertySource
  static void configureDatabase(DynamicPropertyRegistry registry) {
    String url = System.getProperty("postgres.test.url");
    String username = System.getProperty("postgres.test.username", "postgres");
    String password = System.getProperty("postgres.test.password", "");
    if (url == null) {
      postgres = new PostgreSQLContainer<>("postgres:17.11-alpine");
      postgres.start();
      url = postgres.getJdbcUrl();
      username = postgres.getUsername();
      password = postgres.getPassword();
    }
    final String finalUrl = url, finalUser = username, finalPassword = password;
    registry.add("spring.datasource.url", () -> finalUrl);
    registry.add("spring.datasource.username", () -> finalUser);
    registry.add("spring.datasource.password", () -> finalPassword);
    registry.add("spring.datasource.hikari.maximum-pool-size", () -> 4);
    registry.add("spring.datasource.hikari.minimum-idle", () -> 0);
    registry.add("spring.datasource.hikari.schema", () -> schema);
    registry.add("spring.flyway.schemas", () -> schema);
    registry.add("spring.flyway.default-schema", () -> schema);
    registry.add("spring.flyway.locations", () -> "classpath:db/migration,classpath:db/demo");
    registry.add("spring.jpa.properties.hibernate.default_schema", () -> schema);
  }

  @Autowired private DataSource dataSource;

  @Autowired private Flyway flyway;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Autowired private CategoryRepository categoryRepository;

  @Autowired private com.toolrental.domain.tool.port.repository.ToolRepository toolRepository;

  @Autowired private ConfirmReservationUseCase confirmReservationUseCase;

  @Autowired private PlatformTransactionManager transactionManager;

  @Test
  void appliesEverySchemaAndDemoMigrationToAnEmptyPostgresDatabase() {
    assertEquals("302", flyway.info().current().getVersion().getVersion());
    assertEquals(
        54,
        java.util.Arrays.stream(flyway.info().applied())
            .filter(info -> info.getVersion() != null)
            .count());
    assertTrue(flyway.validateWithResult().validationSuccessful);
  }

  @Test
  void loadsAtLeastTwentyDemoRowsIntoEveryBusinessTable() {
    BUSINESS_TABLES.forEach(
        table -> {
          Integer rowCount =
              jdbcTemplate.queryForObject("SELECT count(*) FROM " + table, Integer.class);
          assertTrue(
              rowCount != null && rowCount >= 20, table + " must contain at least twenty rows");
        });
  }

  @Test
  void enforcesDatabaseConstraintsInsteadOfRelyingOnlyOnJpa() {
    UUID categoryId = UUID.randomUUID();

    assertThrows(
        DataIntegrityViolationException.class,
        () ->
            jdbcTemplate.update(
                "INSERT INTO categories (category_id, name) VALUES (?, ?)", categoryId, "   "));
  }

  @Test
  void readsMigratedDataThroughTheRepositoryAdapter() {
    assertTrue(categoryRepository.findAll().size() >= 20);
    assertTrue(toolRepository.findAll().size() >= 50);
  }

  @Test
  void providesCatalogAndHistoricalFixturesWithOneAccountPerPrimaryRole() {
    assertTrue(jdbcTemplate.queryForObject("SELECT count(*) FROM tools", Integer.class) >= 30);
    assertEquals(
        50, jdbcTemplate.queryForObject("SELECT count(*) FROM equipment_units", Integer.class));
    assertEquals(
        3,
        jdbcTemplate.queryForObject(
            """
            SELECT count(*)
            FROM users account
            JOIN user_roles assignment ON assignment.user_id = account.user_id
            JOIN roles role ON role.role_id = assignment.role_id
            WHERE (account.email = 'cliente@toolrental.local' AND role.code = 'CUSTOMER')
               OR (account.email = 'proveedor@toolrental.local' AND role.code = 'SUPPLIER')
               OR (account.email = 'admin@toolrental.local' AND role.code = 'ADMIN')
            """,
            Integer.class));
  }

  @Test
  void rollsBackTheWholeTransactionWhenAnOperationFails() {
    UUID categoryId = UUID.randomUUID();
    var transaction = new TransactionTemplate(transactionManager);

    assertThrows(
        IllegalStateException.class,
        () ->
            transaction.executeWithoutResult(
                status -> {
                  jdbcTemplate.update(
                      "INSERT INTO categories (category_id, name) VALUES (?, ?)",
                      categoryId,
                      "Rollback verification");
                  throw new IllegalStateException("Force rollback");
                }));

    assertEquals(
        0,
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM categories WHERE category_id = ?", Integer.class, categoryId));
  }

  @Test
  void serializesConcurrentInventoryCodeWritesForTheSameSupplier() throws Exception {
    String inventoryCode = "CONCURRENT-001";
    var ready = new CountDownLatch(2);
    var start = new CountDownLatch(1);
    ExecutorService executor = Executors.newFixedThreadPool(2);

    try {
      List<Future<Boolean>> attempts =
          List.of(
              executor.submit(() -> insertEquipmentUnit(inventoryCode, ready, start)),
              executor.submit(() -> insertEquipmentUnit(inventoryCode, ready, start)));

      assertTrue(ready.await(10, TimeUnit.SECONDS));
      start.countDown();

      long successfulWrites =
          attempts.stream()
              .map(PostgreSqlPersistenceIntegrationTest::completedSuccessfully)
              .filter(Boolean::booleanValue)
              .count();

      assertEquals(1, successfulWrites);
      assertEquals(
          1,
          jdbcTemplate.queryForObject(
              "SELECT count(*) FROM equipment_units WHERE inventory_code = ?",
              Integer.class,
              inventoryCode));
    } finally {
      executor.shutdownNow();
      jdbcTemplate.update("DELETE FROM equipment_units WHERE inventory_code = ?", inventoryCode);
    }
  }

  @Test
  void preventsConcurrentConfirmationsFromBookingTheSameEquipmentUnit() throws Exception {
    UUID firstReservationId = UUID.randomUUID();
    UUID secondReservationId = UUID.randomUUID();
    insertPendingReservation(
        firstReservationId, UUID.fromString("00000000-0000-0000-0000-000000000002"));
    insertPendingReservation(
        secondReservationId, UUID.fromString("00000000-0000-0000-0000-000000000003"));

    var ready = new CountDownLatch(2);
    var start = new CountDownLatch(1);
    ExecutorService executor = Executors.newFixedThreadPool(2);

    try {
      List<Future<Boolean>> attempts =
          List.of(
              executor.submit(() -> confirmReservation(firstReservationId, ready, start)),
              executor.submit(() -> confirmReservation(secondReservationId, ready, start)));

      assertTrue(ready.await(10, TimeUnit.SECONDS));
      start.countDown();

      long confirmedAttempts =
          attempts.stream()
              .map(PostgreSqlPersistenceIntegrationTest::completedSuccessfully)
              .filter(Boolean::booleanValue)
              .count();

      assertEquals(1, confirmedAttempts);
      assertEquals(
          1,
          jdbcTemplate.queryForObject(
              """
              SELECT count(*)
              FROM reservations
              WHERE reservation_id IN (?, ?)
                AND reservation_status = 'CONFIRMED'
              """,
              Integer.class,
              firstReservationId,
              secondReservationId));
    } finally {
      executor.shutdownNow();
      jdbcTemplate.update(
          "DELETE FROM reservations WHERE reservation_id IN (?, ?)",
          firstReservationId,
          secondReservationId);
    }
  }

  private void insertPendingReservation(UUID reservationId, UUID customerId) {
    jdbcTemplate.update(
        """
        INSERT INTO reservations (
            reservation_id, customer_id, created_at, start_date, return_date,
            reservation_status, currency, scheduled_fulfillment_at, fulfillment_method,
            cancellation_policy_id, delivery_fee, effective_return_date
        ) VALUES (?, ?, now(), DATE '2030-01-10', DATE '2030-01-12', 'PENDING', 'COP',
            TIMESTAMPTZ '2030-01-10 08:00:00-05', 'CUSTOMER_PICKUP',
            '00000000-0000-0000-0000-000000000203', 0, DATE '2030-01-12')
        """,
        reservationId,
        customerId);
    jdbcTemplate.update(
        """
        INSERT INTO reservation_items (
            reservation_id, equipment_unit_id, agreed_daily_rate
        ) VALUES (?, '30000000-0000-0000-0000-000000000001', 10000.0000)
        """,
        reservationId);
  }

  private boolean confirmReservation(UUID reservationId, CountDownLatch ready, CountDownLatch start)
      throws InterruptedException {
    ready.countDown();
    if (!start.await(10, TimeUnit.SECONDS)) {
      throw new IllegalStateException("Concurrent confirmations did not start in time");
    }

    try {
      confirmReservationUseCase.execute(
          new ReservationId(reservationId),
          new SupplierId(UUID.fromString("00000000-0000-0000-0000-000000000001")));
      return true;
    } catch (AvailabilityConflictApplicationException exception) {
      return false;
    }
  }

  private boolean insertEquipmentUnit(
      String inventoryCode, CountDownLatch ready, CountDownLatch start) throws Exception {
    try (Connection connection = dataSource.getConnection()) {
      connection.setAutoCommit(false);
      ready.countDown();
      if (!start.await(10, TimeUnit.SECONDS)) {
        throw new IllegalStateException("Concurrent inserts did not start in time");
      }

      try (PreparedStatement statement =
          connection.prepareStatement(
              """
              INSERT INTO equipment_units (
                  equipment_unit_id, tool_id, inventory_code,
                  current_condition, operational_status
              ) VALUES (?, '20000000-0000-0000-0000-000000000001', ?, 'GOOD', 'OPERATIONAL')
              """)) {
        statement.setObject(1, UUID.randomUUID());
        statement.setString(2, inventoryCode);
        statement.executeUpdate();
        connection.commit();
        return true;
      } catch (SQLException exception) {
        connection.rollback();
        if ("23505".equals(exception.getSQLState())) {
          return false;
        }
        throw exception;
      }
    }
  }

  private static boolean completedSuccessfully(Future<Boolean> attempt) {
    try {
      return attempt.get(15, TimeUnit.SECONDS);
    } catch (Exception exception) {
      throw new IllegalStateException("Concurrent insert did not complete", exception);
    }
  }
}
