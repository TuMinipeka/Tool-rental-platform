package com.toolrental.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

class FlywayUpgradeIntegrationTest {
  @Test
  void upgradesExistingHistoryWithoutLosingDataOrChangingAppliedChecksums() throws Exception {
    PostgreSQLContainer<?> container = null;
    String url = System.getProperty("postgres.test.url");
    String user = System.getProperty("postgres.test.username", "postgres");
    String password = System.getProperty("postgres.test.password", "");
    if (url == null) {
      container = new PostgreSQLContainer<>("postgres:17.11-alpine");
      container.start();
      url = container.getJdbcUrl();
      user = container.getUsername();
      password = container.getPassword();
    }
    String schema = "upgrade_" + UUID.randomUUID().toString().replace("-", "");
    try {
      Flyway legacy =
          Flyway.configure()
              .dataSource(url, user, password)
              .schemas(schema)
              .defaultSchema(schema)
              .target("200")
              .cleanDisabled(true)
              .load();
      assertThat(legacy.migrate().migrationsExecuted).isEqualTo(25);
      Map<String, Integer> originalChecksums = checksums(legacy);
      UUID id = UUID.randomUUID();
      try (var connection = DriverManager.getConnection(url, user, password)) {
        connection.setSchema(schema);
        try (var insert =
            connection.prepareStatement(
                "INSERT INTO categories(category_id, name, description) VALUES (?, ?, ?)")) {
          insert.setObject(1, id);
          insert.setString(2, "Existing category");
          insert.setString(3, "Preserved before forward migration");
          insert.executeUpdate();
        }
      }
      Flyway upgraded =
          Flyway.configure()
              .dataSource(url, user, password)
              .schemas(schema)
              .defaultSchema(schema)
              .cleanDisabled(true)
              .load();
      assertThat(upgraded.migrate().migrationsExecuted).isEqualTo(11);
      assertThat(upgraded.info().current().getVersion().getVersion()).isEqualTo("304");
      assertThat(checksums(upgraded)).containsAllEntriesOf(originalChecksums);
      assertThat(upgraded.validateWithResult().validationSuccessful).isTrue();
      assertThat(upgraded.migrate().migrationsExecuted).isZero();
      try (var connection = DriverManager.getConnection(url, user, password)) {
        connection.setSchema(schema);
        try (var query =
            connection.prepareStatement(
                "SELECT description FROM categories WHERE category_id = ?")) {
          query.setObject(1, id);
          try (var rows = query.executeQuery()) {
            assertThat(rows.next()).isTrue();
            assertThat(rows.getString(1)).isEqualTo("Preserved before forward migration");
          }
        }
        try (var query = connection.createStatement();
            var rows =
                query.executeQuery(
                    "SELECT count(*) FROM pg_trigger WHERE tgrelid = 'equipment_units'::regclass "
                        + "AND tgname = 'trg_equipment_units_supplier_inventory_code' AND NOT tgisinternal")) {
          rows.next();
          assertThat(rows.getInt(1)).isEqualTo(1);
        }
        try (var query =
            connection.prepareStatement(
                "SELECT count(*) FROM information_schema.tables WHERE table_schema = ? "
                    + "AND table_type = 'BASE TABLE' AND table_name <> 'flyway_schema_history'")) {
          query.setString(1, schema);
          try (var rows = query.executeQuery()) {
            rows.next();
            assertThat(rows.getInt(1)).isEqualTo(25);
          }
        }
      }
    } finally {
      if (container != null) container.stop();
    }
  }

  private static Map<String, Integer> checksums(Flyway flyway) {
    return Arrays.stream(flyway.info().applied())
        .filter(info -> info.getVersion() != null)
        .collect(
            Collectors.toMap(info -> info.getVersion().getVersion(), MigrationInfo::getChecksum));
  }
}
