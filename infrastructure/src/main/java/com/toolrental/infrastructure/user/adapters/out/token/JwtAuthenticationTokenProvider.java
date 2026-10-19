package com.toolrental.infrastructure.user.adapters.out.token;

import com.toolrental.domain.user.model.valueobject.IssuedToken;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.domain.user.port.AuthenticationTokenProvider;
import com.toolrental.domain.usersession.model.valueobject.UserSessionId;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
public final class JwtAuthenticationTokenProvider implements AuthenticationTokenProvider {
  private static final String ISSUER = "tool-rental-platform";
  private static final String ACCESS_PURPOSE = "access";

  private final JwtEncoder encoder;
  private final Duration accessTtl;

  public JwtAuthenticationTokenProvider(
      JwtEncoder encoder, @Value("${auth.jwt.access-ttl:PT15M}") Duration accessTtl) {
    this.encoder = encoder;
    this.accessTtl = accessTtl;
  }

  public IssuedToken issueAccessToken(
      UserId userId, Set<String> roles, UserSessionId sessionId, Instant issuedAt) {
    var expiresAt = issuedAt.plus(accessTtl);
    var claims =
        baseClaims(userId, issuedAt, expiresAt, ACCESS_PURPOSE)
            .claim("roles", roles.stream().sorted().toList())
            .claim("sid", sessionId.value().toString())
            .build();
    return encode(claims, expiresAt);
  }

  private JwtClaimsSet.Builder baseClaims(
      UserId userId, Instant issuedAt, Instant expiresAt, String purpose) {
    return JwtClaimsSet.builder()
        .issuer(ISSUER)
        .subject(userId.value().toString())
        .issuedAt(issuedAt)
        .expiresAt(expiresAt)
        .id(java.util.UUID.randomUUID().toString())
        .claim("purpose", purpose);
  }

  private IssuedToken encode(JwtClaimsSet claims, Instant expiresAt) {
    var header = JwsHeader.with(MacAlgorithm.HS256).build();
    var value = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    return new IssuedToken(value, expiresAt);
  }
}
