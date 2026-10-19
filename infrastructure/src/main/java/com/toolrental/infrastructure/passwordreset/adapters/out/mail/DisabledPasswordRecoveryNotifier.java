package com.toolrental.infrastructure.passwordreset.adapters.out.mail;

import com.toolrental.domain.passwordreset.port.PasswordRecoveryNotifier;
import java.time.Instant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "auth.password-recovery.delivery",
    havingValue = "disabled",
    matchIfMissing = true)
public final class DisabledPasswordRecoveryNotifier implements PasswordRecoveryNotifier {
  public void send(String recipient, String rawToken, Instant expiresAt) {
    // Tests may disable external delivery. Secrets are deliberately neither returned nor logged.
  }
}
