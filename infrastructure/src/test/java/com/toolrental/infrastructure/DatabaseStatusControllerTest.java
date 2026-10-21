package com.toolrental.infrastructure;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.toolrental.infrastructure.common.adapters.in.rest.controllers.DatabaseStatusController;
import com.toolrental.infrastructure.common.adapters.in.rest.dtos.DatabaseStatusResponse;
import com.toolrental.infrastructure.common.monitoring.DatabaseConnectivityProbe;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DatabaseStatusControllerTest {
  @Test
  void returnsSuccessOnlyWhenDatabaseProbeSucceeds() throws Exception {
    var probe = mock(DatabaseConnectivityProbe.class);
    when(probe.check())
        .thenReturn(new DatabaseStatusResponse(true, Instant.parse("2026-10-15T15:00:00Z")));
    MockMvcBuilders.standaloneSetup(new DatabaseStatusController(probe))
        .build()
        .perform(get("/status/database"))
        .andExpect(status().isOk())
        .andExpect(header().string("Cache-Control", "no-store"))
        .andExpect(jsonPath("$.connected").value(true))
        .andExpect(jsonPath("$.password").doesNotExist());
  }

  @Test
  void returnsUnavailableWhenDatabaseProbeFails() throws Exception {
    var probe = mock(DatabaseConnectivityProbe.class);
    when(probe.check()).thenReturn(new DatabaseStatusResponse(false, Instant.now()));
    MockMvcBuilders.standaloneSetup(new DatabaseStatusController(probe))
        .build()
        .perform(get("/status/database"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.connected").value(false));
  }
}
