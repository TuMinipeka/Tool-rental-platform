package com.toolrental.infrastructure.user.adapters.out.password;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.user.port.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptPasswordHasher implements PasswordHasher {
  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  public String hash(String password) {
    if (password == null
        || password.length() < 12
        || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
      throw new BusinessRuleException(
          "Password must contain at least 12 characters and at most 72 UTF-8 bytes");
    return encoder.encode(password);
  }
}
