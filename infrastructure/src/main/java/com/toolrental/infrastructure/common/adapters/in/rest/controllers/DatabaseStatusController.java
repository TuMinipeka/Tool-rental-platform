package com.toolrental.infrastructure.common.adapters.in.rest.controllers;

import com.toolrental.infrastructure.common.adapters.in.rest.dtos.DatabaseStatusResponse;
import com.toolrental.infrastructure.common.monitoring.DatabaseConnectivityProbe;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SecurityRequirements
public final class DatabaseStatusController {
  private final DatabaseConnectivityProbe probe;

  public DatabaseStatusController(DatabaseConnectivityProbe probe) {
    this.probe = probe;
  }

  @GetMapping("/status/database")
  public ResponseEntity<DatabaseStatusResponse> check() {
    var result = probe.check();
    return ResponseEntity.status(
            result.connected() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
        .cacheControl(CacheControl.noStore())
        .body(result);
  }
}
