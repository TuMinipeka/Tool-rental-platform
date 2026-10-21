package com.toolrental.infrastructure.user.adapters.in.rest.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;

public record LoginRequest(
    @NotBlank @Email @Size(max = 320) @Pattern(regexp = "^[^\\p{C}\\s<>\"\\\\]+$") String email,
    @NotBlank @Size(max = 72) String password) {
  public LoginRequest {
    email = email == null ? null : email.trim();
    if (password != null
        && (password.indexOf('\0') >= 0 || password.getBytes(StandardCharsets.UTF_8).length > 72))
      throw new IllegalArgumentException("Invalid login input");
  }

  @Override
  public String toString() {
    return "LoginRequest[credentials=REDACTED]";
  }
}
