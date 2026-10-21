package com.toolrental.infrastructure.user.adapters.out.token;

import com.toolrental.domain.user.model.valueobject.SecretToken;
import com.toolrental.domain.user.port.SecureTokenService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

@Component
public final class SecureRandomTokenService implements SecureTokenService {
  private final SecureRandom random = new SecureRandom();

  public SecretToken issue() {
    var bytes = new byte[32];
    random.nextBytes(bytes);
    var raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    return new SecretToken(raw, hash(raw));
  }

  public String hash(String rawToken) {
    if (rawToken == null || rawToken.isBlank()) return "0".repeat(64);
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256")
                  .digest(rawToken.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }
}
