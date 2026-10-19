package com.toolrental.infrastructure.user.adapters.in.rest.controllers;

import com.toolrental.application.user.command.RegisterAccountCommand;
import com.toolrental.application.user.dto.AuthenticationResponse;
import com.toolrental.application.user.dto.PasswordRecoveryResponse;
import com.toolrental.application.user.usecase.AuthenticateUserUseCase;
import com.toolrental.application.user.usecase.RecoverPasswordUseCase;
import com.toolrental.application.user.usecase.RegisterCustomerAccountUseCase;
import com.toolrental.infrastructure.user.adapters.in.rest.dtos.AccountRegistrationRequest;
import com.toolrental.infrastructure.user.adapters.in.rest.dtos.LoginRequest;
import com.toolrental.infrastructure.user.adapters.in.rest.dtos.PasswordRecoveryRequest;
import com.toolrental.infrastructure.user.adapters.in.rest.dtos.PasswordResetRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public final class AuthenticationController {
  private final AuthenticateUserUseCase authentication;
  private final RegisterCustomerAccountUseCase registration;
  private final RecoverPasswordUseCase recovery;

  public AuthenticationController(
      AuthenticateUserUseCase authentication,
      RegisterCustomerAccountUseCase registration,
      RecoverPasswordUseCase recovery) {
    this.authentication = authentication;
    this.registration = registration;
    this.recovery = recovery;
  }

  @PostMapping("/login")
  public AuthenticationResponse login(@Valid @RequestBody LoginRequest request) {
    return authentication.execute(request.email(), request.password());
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthenticationResponse register(@Valid @RequestBody AccountRegistrationRequest request) {
    return registration.execute(
        new RegisterAccountCommand(
            request.email(),
            request.displayName(),
            request.password(),
            request.personType(),
            request.documentType(),
            request.documentNumber(),
            request.billingName()));
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

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout() {
    // Access tokens are stateless and expire quickly; the client discards the token.
  }
}
