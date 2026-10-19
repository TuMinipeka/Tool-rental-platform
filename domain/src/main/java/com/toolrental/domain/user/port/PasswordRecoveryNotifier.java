package com.toolrental.domain.user.port;

import java.time.Instant;

public interface PasswordRecoveryNotifier {
  void send(String recipient, String rawToken, Instant expiresAt);
}
