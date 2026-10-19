package com.toolrental.infrastructure.user.adapters.out.token;

import com.toolrental.domain.user.model.valueobject.IssuedToken;
import com.toolrental.domain.user.model.valueobject.PasswordResetSubject;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.domain.user.port.AuthenticationTokenProvider;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
public final class JwtAuthenticationTokenProvider implements AuthenticationTokenProvider {
  private static final String ISSUER = "tool-rental-platform";
  private static final String ACCESS_PURPOSE = "access";
  private static final String RESET_PURPOSE = "password-reset";

  private final JwtEncoder encoder;
  private final JwtDecoder decoder;
  private final Duration accessTtl;
  private final Duration resetTtl;

  public JwtAuthenticationTokenProvider(
      JwtEncoder encoder,
      @Qualifier("passwordResetJwtDecoder") JwtDecoder decoder,
      @Value("${auth.jwt.access-ttl:PT15M}") Duration accessTtl,
      @Value("${auth.jwt.password-reset-ttl:PT15M}") Duration resetTtl) {
    this.encoder = encoder;
    this.decoder = decoder;
    this.accessTtl = accessTtl;
    this.resetTtl = resetTtl;
  }

  public IssuedToken issueAccessToken(UserId userId, Set<String> roles, Instant issuedAt) {
    var expiresAt = issuedAt.plus(accessTtl);
    var claims =
        baseClaims(userId, issuedAt, expiresAt, ACCESS_PURPOSE)
            .claim("roles", roles.stream().sorted().toList())
            .build();
    return encode(claims, expiresAt);
  }

  public IssuedToken issuePasswordResetToken(UserId userId, String passwordHash, Instant issuedAt) {
    var expiresAt = issuedAt.plus(resetTtl);
    var claims =
        baseClaims(userId, issuedAt, expiresAt, RESET_PURPOSE)
            .claim("passwordFingerprint", fingerprint(passwordHash))
            .build();
    return encode(claims, expiresAt);
  }

  public PasswordResetSubject verifyPasswordResetToken(String token) {
    var jwt = decoder.decode(token);
    if (!RESET_PURPOSE.equals(jwt.getClaimAsString("purpose"))) {
      throw new IllegalArgumentException("Unexpected token purpose");
    }
    return new PasswordResetSubject(
        new UserId(UUID.fromString(jwt.getSubject())), jwt.getClaimAsString("passwordFingerprint"));
  }

  public boolean matchesCurrentPassword(PasswordResetSubject subject, String currentPasswordHash) {
    return MessageDigest.isEqual(
        subject.passwordFingerprint().getBytes(StandardCharsets.UTF_8),
        fingerprint(currentPasswordHash).getBytes(StandardCharsets.UTF_8));
  }

  private JwtClaimsSet.Builder baseClaims(
      UserId userId, Instant issuedAt, Instant expiresAt, String purpose) {
    return JwtClaimsSet.builder()
        .issuer(ISSUER)
        .subject(userId.value().toString())
        .issuedAt(issuedAt)
        .expiresAt(expiresAt)
        .id(UUID.randomUUID().toString())
        .claim("purpose", purpose);
  }

  private IssuedToken encode(JwtClaimsSet claims, Instant expiresAt) {
    var header = JwsHeader.with(MacAlgorithm.HS256).build();
    var value = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    return new IssuedToken(value, expiresAt);
  }

  private String fingerprint(String passwordHash) {
    try {
      var digest = MessageDigest.getInstance("SHA-256");
      return Base64.getUrlEncoder()
          .withoutPadding()
          .encodeToString(digest.digest(passwordHash.getBytes(StandardCharsets.UTF_8)));
    } catch (java.security.NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }
}
