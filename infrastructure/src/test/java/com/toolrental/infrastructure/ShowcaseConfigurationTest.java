package com.toolrental.infrastructure;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.toolrental.infrastructure.common.config.ShowcaseConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class ShowcaseConfigurationTest {
  @Test
  void rejectsShowcaseWithoutProductionSecurity() {
    var environment = new MockEnvironment();
    environment.setActiveProfiles("showcase", "local");
    assertThrows(IllegalStateException.class, () -> new ShowcaseConfiguration(environment));
  }

  @Test
  void rejectsTheWorkingDatabaseEvenWithProductionSecurity() {
    var environment = new MockEnvironment();
    environment.setActiveProfiles("production", "showcase");
    environment.withProperty("SHOWCASE_DATABASE_NAME", "tool_rental_showcase");
    environment.withProperty("DB_URL", "jdbc:postgresql://postgres:5432/tool_rental");
    assertThrows(IllegalStateException.class, () -> new ShowcaseConfiguration(environment));
  }

  @Test
  void acceptsAnExplicitlyIsolatedDatabaseWithProductionSecurity() {
    var environment = new MockEnvironment();
    environment.setActiveProfiles("production", "showcase");
    environment.withProperty("SHOWCASE_DATABASE_NAME", "tool_rental_showcase");
    environment.withProperty("DB_URL", "jdbc:postgresql://postgres:5432/tool_rental_showcase");
    assertDoesNotThrow(() -> new ShowcaseConfiguration(environment));
  }
}
