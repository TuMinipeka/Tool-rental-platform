package com.toolrental.infrastructure.usersession.adapters.in.rest.controllers;

import com.toolrental.application.user.dto.AuthenticationResponse;
import com.toolrental.application.usersession.dto.UserSessionResponse;
import com.toolrental.application.usersession.usecase.ManageUserSessionUseCase;
import com.toolrental.application.usersession.usecase.RefreshSessionUseCase;
import com.toolrental.domain.usersession.model.valueobject.UserSessionId;
import com.toolrental.infrastructure.common.config.OpenApiConfiguration;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.usersession.adapters.in.rest.RefreshCookieFactory;
import com.toolrental.infrastructure.usersession.adapters.in.rest.dtos.CsrfResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/auth", "/api/auth"})
@SecurityRequirements
public final class UserSessionController {
  private final RefreshSessionUseCase refresh;
  private final ManageUserSessionUseCase sessions;
  private final RefreshCookieFactory cookies;

  public UserSessionController(
      RefreshSessionUseCase refresh,
      ManageUserSessionUseCase sessions,
      RefreshCookieFactory cookies) {
    this.refresh = refresh;
    this.sessions = sessions;
    this.cookies = cookies;
  }

  @GetMapping("/csrf")
  public CsrfResponse csrf(CsrfToken token) {
    return new CsrfResponse(token.getHeaderName(), token.getParameterName(), token.getToken());
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthenticationResponse> refresh(
      @CookieValue(name = "trp_refresh", required = false) String refreshToken) {
    var result = refresh.execute(refreshToken);
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookies.issue(result.refreshToken()).toString())
        .body(result.authentication());
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @CookieValue(name = "trp_refresh", required = false) String refreshToken) {
    sessions.logout(refreshToken);
    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, cookies.clear().toString())
        .build();
  }

  @PostMapping("/logout-all")
  @SecurityRequirement(name = OpenApiConfiguration.BEARER_SCHEME)
  public ResponseEntity<Void> logoutAll(JwtAuthenticationToken principal) {
    sessions.logoutAll(Actor.id(principal));
    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, cookies.clear().toString())
        .build();
  }

  @GetMapping("/sessions")
  @SecurityRequirement(name = OpenApiConfiguration.BEARER_SCHEME)
  public List<UserSessionResponse> sessions(JwtAuthenticationToken principal) {
    return sessions.list(
        Actor.id(principal), UUID.fromString(principal.getToken().getClaimAsString("sid")));
  }

  @DeleteMapping("/sessions/{id}")
  @SecurityRequirement(name = OpenApiConfiguration.BEARER_SCHEME)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void revokeSession(JwtAuthenticationToken principal, @PathVariable UUID id) {
    sessions.revoke(Actor.id(principal), new UserSessionId(id));
  }
}
