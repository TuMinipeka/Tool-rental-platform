package com.toolrental.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("local")
class PostgreSqlPersistenceIntegrationTest {
  private static final List<String> BUSINESS_TABLES =
      List.of(
          "users",
          "roles",
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
          "invoice_items");

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:18.6-alpine")
          .withDatabaseName("tool_rental")
          .withUsername("tool_rental")
          .withPassword("tool_rental");

  @DynamicPropertySource
  static void configureDatabase(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.datasource.hikari.maximum-pool-size", () -> 4);
    registry.add("spring.datasource.hikari.minimum-idle", () -> 1);
  }

  @Autowired private Flyway flyway;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void appliesEverySchemaAndDemoMigrationToAnEmptyPostgresDatabase() {
    assertEquals("118", flyway.info().current().getVersion().getVersion());
    assertEquals(37, flyway.info().applied().length);
    assertTrue(flyway.validateWithResult().validationSuccessful);
  }

  @Test
  void loadsAtLeastFiveDemoRowsIntoEveryBusinessTable() {
    BUSINESS_TABLES.forEach(
        table -> {
          Integer rowCount =
              jdbcTemplate.queryForObject("SELECT count(*) FROM " + table, Integer.class);
          assertTrue(rowCount != null && rowCount >= 5, table + " must contain at least five rows");
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
}
