package com.toolrental.infrastructure.common.config;

import java.util.Arrays;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration(proxyBeanMethods = false)
@Profile("showcase")
public class ShowcaseConfiguration {
  private final String database;

  public ShowcaseConfiguration(Environment environment) {
    var profiles = Arrays.asList(environment.getActiveProfiles());
    if (!profiles.contains("production") || profiles.contains("local")) {
      throw new IllegalStateException("Showcase requires production security and excludes local");
    }
    database = environment.getRequiredProperty("SHOWCASE_DATABASE_NAME");
    if (!database.matches("[a-z][a-z0-9_]*showcase[a-z0-9_]*")
        || !environment.getRequiredProperty("DB_URL").endsWith("/" + database)) {
      throw new IllegalStateException("Showcase requires an explicitly isolated showcase database");
    }
  }

  @Bean
  public FlywayMigrationStrategy showcaseMigrationStrategy() {
    return flyway -> {
      // This strategy is resolved before Flyway migrates, including credential deactivation.
      try (var connection = flyway.getConfiguration().getDataSource().getConnection();
          var statement = connection.prepareStatement("SELECT current_database()");
          var rows = statement.executeQuery()) {
        if (!rows.next() || !database.equals(rows.getString(1))) {
          throw new IllegalStateException("Showcase migration target is not the isolated database");
        }
      } catch (java.sql.SQLException failure) {
        throw new IllegalStateException("Showcase database verification failed", failure);
      }
      flyway.migrate();
    };
  }
}
