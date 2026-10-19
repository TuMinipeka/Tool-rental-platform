package com.toolrental.infrastructure.user.adapters.in.rest.controllers;

import com.toolrental.application.user.command.RegisterAccountCommand;
import com.toolrental.application.user.dto.AuthenticationResponse;
import com.toolrental.application.user.dto.PasswordRecoveryResponse;
import com.toolrental.application.user.usecase.AuthenticateUserUseCase;
import com.toolrental.application.user.usecase.RecoverPasswordUseCase;
import com.toolrental.application.user.usecase.RegisterCustomerAccountUseCase;
import com.toolrental.application.usersession.dto.UserSessionResponse;
import com.toolrental.application.usersession.usecase.ManageUserSessionUseCase;
import com.toolrental.application.usersession.usecase.RefreshSessionUseCase;
import com.toolrental.domain.usersession.model.valueobject.UserSessionId;
import com.toolrental.infrastructure.common.config.OpenApiConfiguration;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.user.adapters.in.rest.dtos.AccountRegistrationRequest;
import com.toolrental.infrastructure.user.adapters.in.rest.dtos.CsrfResponse;
import com.toolrental.infrastructure.user.adapters.in.rest.dtos.LoginRequest;
import com.toolrental.infrastructure.user.adapters.in.rest.dtos.PasswordRecoveryRequest;
import com.toolrental.infrastructure.user.adapters.in.rest.dtos.PasswordResetRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/auth", "/api/auth"})
@SecurityRequirements
public final class AuthenticationController {
  private final AuthenticateUserUseCase authentication;
  private final RegisterCustomerAccountUseCase registration;
  private final RecoverPasswordUseCase recovery;
  private final RefreshSessionUseCase refresh;
  private final ManageUserSessionUseCase sessions;
  private final Duration refreshTtl;
  private final boolean secureCookie;
  private final String sameSite;

  public AuthenticationController(
      AuthenticateUserUseCase authentication,
      RegisterCustomerAccountUseCase registration,
      RecoverPasswordUseCase recovery,
      RefreshSessionUseCase refresh,
      ManageUserSessionUseCase sessions,
      @Value("${auth.jwt.refresh-ttl:P30D}") Duration refreshTtl,
      @Value("${auth.cookie.secure:false}") boolean secureCookie,
      @Value("${auth.cookie.same-site:Lax}") String sameSite) {
    this.authentication = authentication;
    this.registration = registration;
    this.recovery = recovery;
    this.refresh = refresh;
    this.sessions = sessions;
    this.refreshTtl = refreshTtl;
    this.secureCookie = secureCookie;
    this.sameSite = sameSite;
  }

  @PostMapping("/login")
  public ResponseEntity<AuthenticationResponse> login(
      HttpServletRequest http, @Valid @RequestBody LoginRequest request) {
    var result = authentication.execute(request.email(), request.password(), clientLabel(http));
    return authenticated(result);
  }

  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(
      HttpServletRequest http, @Valid @RequestBody AccountRegistrationRequest request) {
    var result =
        registration.execute(
            new RegisterAccountCommand(
                request.email(),
                request.displayName(),
                request.password(),
                request.personType(),
                request.documentType(),
                request.documentNumber(),
                request.billingName()),
            clientLabel(http));
    return ResponseEntity.status(HttpStatus.CREATED)
        .header(HttpHeaders.SET_COOKIE, refreshCookie(result.refreshToken()).toString())
        .body(result.authentication());
  }

  @PostMapping("/password-recovery")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public PasswordRecoveryResponse requestPasswordRecovery(
      @Valid @RequestBody PasswordRecoveryRequest request) {
    return recovery.request(request.email());
  }

  @PostMapping("/password-reset")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void resetPassword(@Valid @RequestBody PasswordResetRequest request) {
    recovery.reset(request.token(), request.newPassword());
  }

  @GetMapping("/csrf")
  public CsrfResponse csrf(CsrfToken token) {
    return new CsrfResponse(token.getHeaderName(), token.getParameterName(), token.getToken());
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthenticationResponse> refresh(
      @CookieValue(name = "trp_refresh", required = false) String refreshToken) {
    var result = refresh.execute(refreshToken);
    return authenticated(result);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @CookieValue(name = "trp_refresh", required = false) String refreshToken) {
    sessions.logout(refreshToken);
    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
        .build();
  }

  @PostMapping("/logout-all")
  @SecurityRequirement(name = OpenApiConfiguration.BEARER_SCHEME)
  public ResponseEntity<Void> logoutAll(JwtAuthenticationToken principal) {
    sessions.logoutAll(Actor.id(principal));
    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
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

  private ResponseEntity<AuthenticationResponse> authenticated(
      com.toolrental.application.user.dto.AuthenticatedSession result) {
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, refreshCookie(result.refreshToken()).toString())
        .body(result.authentication());
  }

  private ResponseCookie refreshCookie(String value) {
    return ResponseCookie.from("trp_refresh", value)
        .httpOnly(true)
        .secure(secureCookie)
        .sameSite(sameSite)
        .path("/api")
        .maxAge(refreshTtl)
        .build();
  }

  private ResponseCookie clearRefreshCookie() {
    return ResponseCookie.from("trp_refresh", "")
        .httpOnly(true)
        .secure(secureCookie)
        .sameSite(sameSite)
        .path("/api")
        .maxAge(0)
        .build();
  }

  private String clientLabel(HttpServletRequest request) {
    var value = request.getHeader("User-Agent");
    if (value == null || value.isBlank()) return null;
    return value.substring(0, Math.min(value.length(), 300));
  }
}
