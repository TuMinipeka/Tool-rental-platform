package com.toolrental.infrastructure.common.monitoring;

import com.toolrental.infrastructure.common.adapters.in.rest.dtos.DatabaseStatusResponse;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import javax.sql.DataSource;
import org.springframework.stereotype.Component;

@Component
public final class DatabaseConnectivityProbe {
  private final DataSource dataSource;
  private final Clock clock;

  public DatabaseConnectivityProbe(DataSource dataSource, Clock clock) {
    this.dataSource = dataSource;
    this.clock = clock;
  }

  public DatabaseStatusResponse check() {
    boolean connected = false;
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement("SELECT 1")) {
      statement.setQueryTimeout(3);
      try (var result = statement.executeQuery()) {
        connected = result.next() && result.getInt(1) == 1;
      }
    } catch (SQLException exception) {
      // Deliberately omit database metadata, SQL errors, and credentials from the response.
      connected = false;
    }
    return new DatabaseStatusResponse(connected, Instant.now(clock));
  }
}
