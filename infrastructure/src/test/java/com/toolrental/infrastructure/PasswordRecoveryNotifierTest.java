package com.toolrental.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.toolrental.infrastructure.passwordreset.adapters.out.mail.SmtpPasswordRecoveryNotifier;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class PasswordRecoveryNotifierTest {
  @Test
  void sendsOnlyTheRecipientAnExpiringSingleUseLink() {
    var sender = Mockito.mock(JavaMailSender.class);
    var notifier =
        new SmtpPasswordRecoveryNotifier(
            sender, "no-reply@example.com", "https://example.com/password-reset");
    notifier.send(
        "customer@example.com", "opaque-test-token", Instant.parse("2030-01-01T00:15:00Z"));

    var captured = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(sender).send(captured.capture());
    var message = captured.getValue();
    assertThat(message.getFrom()).isEqualTo("no-reply@example.com");
    assertThat(message.getTo()).containsExactly("customer@example.com");
    assertThat(message.getText())
        .contains("https://example.com/password-reset?token=opaque-test-token")
        .contains("2030-01-01T00:15:00Z");
  }
}
