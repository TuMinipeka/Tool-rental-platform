package com.toolrental.infrastructure.common.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.toolrental.domain.usersession.port.repository.UserSessionRepository;
import com.toolrental.infrastructure.usersession.adapters.out.token.SessionJwtValidator;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {
  @Bean
  @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
  SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthenticationConverter jwtAuthenticationConverter,
      @Qualifier("accessJwtDecoder") JwtDecoder accessJwtDecoder,
      com.toolrental.infrastructure.common.web.SecurityProblemWriter problems,
      @Value("${auth.cookie.secure:false}") boolean secureCookie,
      @Value("${auth.cookie.same-site:Lax}") String sameSite)
      throws Exception {
    return http.csrf(
            csrf -> {
              var repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
              repository.setCookiePath("/api");
              repository.setCookieCustomizer(
                  cookie -> cookie.secure(secureCookie).sameSite(sameSite));
              csrf.csrfTokenRepository(repository)
                  .requireCsrfProtectionMatcher(
                      request -> {
                        var path = request.getRequestURI();
                        return "POST".equals(request.getMethod())
                            && (path.endsWith("/auth/refresh")
                                || path.endsWith("/auth/logout")
                                || path.endsWith("/auth/logout-all"));
                      });
            })
        .cors(cors -> {})
        .sessionManagement(
            sessions -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            exceptions ->
                exceptions.authenticationEntryPoint(problems).accessDeniedHandler(problems))
        .authorizeHttpRequests(
            requests ->
                requests
                    .requestMatchers(
                        "/",
                        "/index.html",
                        "/connection-status.css",
                        "/connection-status.js",
                        "/status/database",
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/password-recovery",
                        "/api/auth/password-reset",
                        "/api/auth/refresh",
                        "/api/auth/logout",
                        "/api/auth/csrf",
                        "/api/v1/auth/login",
                        "/api/v1/auth/register",
                        "/api/v1/auth/password-recovery",
                        "/api/v1/auth/password-reset",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/logout",
                        "/api/v1/auth/csrf",
                        "/api/public/**",
                        "/api/v1/public/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            resource ->
                resource.jwt(
                    jwt ->
                        jwt.decoder(accessJwtDecoder)
                            .jwtAuthenticationConverter(jwtAuthenticationConverter)))
        .build();
  }

  @Bean
  JwtEncoder jwtEncoder(@Value("${auth.jwt.secret}") String secret) {
    requireStrongSecret(secret);
    return new NimbusJwtEncoder(new ImmutableSecret<>(secret.getBytes(StandardCharsets.UTF_8)));
  }

  @Bean
  JwtDecoder accessJwtDecoder(
      @Value("${auth.jwt.secret}") String secret, UserSessionRepository sessions, Clock clock) {
    return jwtDecoder(secret, "access", new SessionJwtValidator(sessions, clock));
  }

  private JwtDecoder jwtDecoder(
      String secret, String purpose, OAuth2TokenValidator<Jwt> sessionValidator) {
    requireStrongSecret(secret);
    var key =
        new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), MacAlgorithm.HS256.getName());
    var decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefaultWithIssuer("tool-rental-platform"),
            new JwtClaimValidator<String>("purpose", purpose::equals),
            sessionValidator));
    return decoder;
  }

  @Bean
  JwtAuthenticationConverter jwtAuthenticationConverter() {
    var authorities = new JwtGrantedAuthoritiesConverter();
    authorities.setAuthoritiesClaimName("roles");
    authorities.setAuthorityPrefix("ROLE_");
    var converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(authorities);
    return converter;
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(
      @Value("${app.cors.allowed-origins:http://localhost:5173}") List<String> origins) {
    var configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(origins);
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(
        List.of("Authorization", "Content-Type", "Idempotency-Key", "X-XSRF-TOKEN", "X-Trace-Id"));
    configuration.setExposedHeaders(List.of("Location", "Deprecation", "Link", "X-Trace-Id"));
    configuration.setAllowCredentials(true);
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  private static void requireStrongSecret(String secret) {
    if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
      throw new IllegalStateException("JWT secret must contain at least 32 UTF-8 bytes");
    }
  }
}
