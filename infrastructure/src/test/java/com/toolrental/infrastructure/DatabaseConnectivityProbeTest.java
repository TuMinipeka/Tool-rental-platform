package com.toolrental.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.toolrental.infrastructure.common.monitoring.DatabaseConnectivityProbe;
import java.sql.*;
import java.time.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class DatabaseConnectivityProbeTest {
  private final Clock clock = Clock.fixed(Instant.parse("2026-10-15T15:00:00Z"), ZoneOffset.UTC);

  @Test
  void queriesTheDatabaseAndClosesEveryResource() throws Exception {
    var source = mock(DataSource.class);
    var connection = mock(Connection.class);
    var statement = mock(PreparedStatement.class);
    var rows = mock(ResultSet.class);
    when(source.getConnection()).thenReturn(connection);
    when(connection.prepareStatement("SELECT 1")).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(rows);
    when(rows.next()).thenReturn(true);
    when(rows.getInt(1)).thenReturn(1);
    var status = new DatabaseConnectivityProbe(source, clock).check();
    assertThat(status.connected()).isTrue();
    assertThat(status.checkedAt()).isEqualTo(clock.instant());
    verify(statement).setQueryTimeout(3);
    verify(rows).close();
    verify(statement).close();
    verify(connection).close();
  }

  @Test
  void reportsUnavailableWithoutLeakingSqlErrors() throws Exception {
    var source = mock(DataSource.class);
    when(source.getConnection()).thenThrow(new SQLException("Secret connection details"));
    var status = new DatabaseConnectivityProbe(source, clock).check();
    assertThat(status.connected()).isFalse();
    assertThat(status.toString()).doesNotContain("Secret");
  }
}
