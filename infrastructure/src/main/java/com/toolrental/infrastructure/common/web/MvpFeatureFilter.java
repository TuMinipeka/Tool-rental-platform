package com.toolrental.infrastructure.common.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public final class MvpFeatureFilter extends OncePerRequestFilter {
  private final boolean recovery;
  private final boolean internal;
  private final ObjectMapper json;

  public MvpFeatureFilter(
      @Value("${auth.password-recovery.enabled:false}") boolean recovery,
      @Value("${payments.internal-enabled:false}") boolean internal,
      ObjectMapper json) {
    this.recovery = recovery;
    this.internal = internal;
    this.json = json;
  }

  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    var path = request.getRequestURI().replaceFirst("^/api/v1/", "/api/");
    boolean disabled =
        (!recovery
                && (path.equals("/api/auth/password-recovery")
                    || path.equals("/api/auth/password-reset")))
            || (!internal
                && (path.startsWith("/api/payments/sandbox/")
                    || path.equals("/api/payments/simulations")
                    || path.equals("/api/payments/internal/checkouts")
                    || ("POST".equals(request.getMethod()) && path.equals("/api/payments"))
                    || ("PATCH".equals(request.getMethod())
                        && path.matches("/api/payments/[^/]+/status"))));
    if (disabled) {
      response.setStatus(404);
      response.setContentType("application/problem+json");
      json.writeValue(
          response.getOutputStream(),
          Map.of(
              "code",
              "FEATURE_DISABLED",
              "status",
              404,
              "title",
              "Not Found",
              "detail",
              "This capability is outside the current MVP",
              "instance",
              request.getRequestURI(),
              "traceId",
              UUID.randomUUID().toString()));
      return;
    }
    chain.doFilter(request, response);
  }
}
