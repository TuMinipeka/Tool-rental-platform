package com.toolrental.infrastructure.passwordreset.adapters.out.mail;

import com.toolrental.domain.user.port.PasswordRecoveryNotifier;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@ConditionalOnProperty(name = "auth.password-recovery.delivery", havingValue = "smtp")
public final class SmtpPasswordRecoveryNotifier implements PasswordRecoveryNotifier {
  private final JavaMailSender mail;
  private final String sender;
  private final String resetUrl;

  public SmtpPasswordRecoveryNotifier(
      JavaMailSender mail,
      @Value("${auth.password-recovery.sender}") String sender,
      @Value("${auth.password-recovery.reset-url}") String resetUrl) {
    this.mail = mail;
    this.sender = sender;
    this.resetUrl = resetUrl;
  }

  public void send(String recipient, String rawToken, Instant expiresAt) {
    var link =
        UriComponentsBuilder.fromUriString(resetUrl)
            .queryParam("token", rawToken)
            .build()
            .toUriString();
    var message = new SimpleMailMessage();
    message.setFrom(sender);
    message.setTo(recipient);
    message.setSubject("Reset your Tool Rental Platform password");
    message.setText(
        "Use this single-use link before "
            + expiresAt
            + ":\n\n"
            + link
            + "\n\nIf you did not request it, ignore this message.");
    mail.send(message);
  }
}
