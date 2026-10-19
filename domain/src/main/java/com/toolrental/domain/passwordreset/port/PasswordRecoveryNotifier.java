package com.toolrental.domain.passwordreset.port;

import java.time.Instant;

public interface PasswordRecoveryNotifier {
  void send(String recipient, String rawToken, Instant expiresAt);
}
