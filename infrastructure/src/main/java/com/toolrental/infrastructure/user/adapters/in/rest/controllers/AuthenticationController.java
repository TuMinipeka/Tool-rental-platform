package com.toolrental.infrastructure.user.adapters.in.rest.controllers;

import com.toolrental.application.user.command.RegisterAccountCommand;
import com.toolrental.application.user.dto.AuthenticationResponse;
import com.toolrental.application.user.usecase.AuthenticateUserUseCase;
import com.toolrental.application.user.usecase.RegisterCustomerAccountUseCase;
import com.toolrental.infrastructure.user.adapters.in.rest.dtos.AccountRegistrationRequest;
import com.toolrental.infrastructure.user.adapters.in.rest.dtos.LoginRequest;
import com.toolrental.infrastructure.usersession.adapters.in.rest.RefreshCookieFactory;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/auth", "/api/auth"})
@SecurityRequirements
public final class AuthenticationController {
  private final AuthenticateUserUseCase authentication;
  private final RegisterCustomerAccountUseCase registration;
  private final RefreshCookieFactory cookies;

  public AuthenticationController(
      AuthenticateUserUseCase authentication,
      RegisterCustomerAccountUseCase registration,
      RefreshCookieFactory cookies) {
    this.authentication = authentication;
    this.registration = registration;
    this.cookies = cookies;
  }

  @PostMapping("/login")
  public ResponseEntity<AuthenticationResponse> login(
      HttpServletRequest http, @Valid @RequestBody LoginRequest request) {
    var result = authentication.execute(request.email(), request.password(), clientLabel(http));
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookies.issue(result.refreshToken()).toString())
        .body(result.authentication());
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
        .header(HttpHeaders.SET_COOKIE, cookies.issue(result.refreshToken()).toString())
        .body(result.authentication());
  }

  private String clientLabel(HttpServletRequest request) {
    var value = request.getHeader("User-Agent");
    if (value == null || value.isBlank()) return null;
    return value.substring(0, Math.min(value.length(), 300));
  }
}
