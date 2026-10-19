package com.toolrental.infrastructure.passwordreset.adapters.in.rest.controllers;

import com.toolrental.application.passwordreset.dto.PasswordRecoveryResponse;
import com.toolrental.application.passwordreset.usecase.RecoverPasswordUseCase;
import com.toolrental.infrastructure.passwordreset.adapters.in.rest.dtos.PasswordRecoveryRequest;
import com.toolrental.infrastructure.passwordreset.adapters.in.rest.dtos.PasswordResetRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/auth", "/api/auth"})
@SecurityRequirements
public final class PasswordRecoveryController {
  private final RecoverPasswordUseCase recovery;

  public PasswordRecoveryController(RecoverPasswordUseCase recovery) {
    this.recovery = recovery;
  }

  @PostMapping("/password-recovery")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public PasswordRecoveryResponse request(@Valid @RequestBody PasswordRecoveryRequest request) {
    return recovery.request(request.email());
  }

  @PostMapping("/password-reset")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void reset(@Valid @RequestBody PasswordResetRequest request) {
    recovery.reset(request.token(), request.newPassword());
  }
}
